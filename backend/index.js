// Imports
require("dotenv").config();
const express = require("express");
const path = require("path");
const axios = require("axios").default;
const fs = require("fs");
const session = require("express-session");
const { pool } = require("./dbConfig");
const bcrypt = require("bcrypt");
const passport = require("passport");
const initializePassport = require("./passportConfig");
const flatCache = require("flat-cache");
const cache = flatCache.load("cache1");
const moment = require("moment");

initializePassport(passport);

// Initialisation
const app = express();

// Middleware
app.use(express.urlencoded({ extended: false }));
app.use(express.json());
app.use((req, res, next) => {
    let d = new Date();
    console.log(
        `${d.getHours()}:${d.getMinutes()}.${d.getSeconds()}  -  ${
            req.method
        }  -  ${req.protocol}://${req.get("host")}${req.originalUrl}`
    );
    next();
});
app.use(
    session({
        secret: process.env.SESSION_SECRET,
        saveUninitialized: false,
        resave: false,
    })
);
app.use(passport.initialize());
app.use(passport.session());

// Caching
const lessThanOneHourAgo = (date) => {
    const now = moment();
    return moment(date).isAfter(now.subtract(1, "h"));
};

const cacher = (req, res, next) => {
    let thisCache = cache.getKey(req.originalUrl);
    if (thisCache != null) {
        console.log(
            `less than one hour ago bool: ${lessThanOneHourAgo(thisCache.date)}`
        );
        if (!lessThanOneHourAgo(thisCache.date)) {
            console.log("cache too old, refetching");
            return next();
        }
        console.log("data fetched from cache");
        return res.send(thisCache.data);
    }
    console.log("no cache, fetching");
    next();
};

// Auth check helper functions
const blockAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) {
        return res.redirect("/api/logoutError");
    }
    next();
};

const blockNotAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) {
        return next();
    }

    res.redirect("/api/loginError");
};

// Routes
app.get("/", (req, res) => {
    res.send({ success: "true" });
});

app.get("/api/jobs", blockNotAuthenticated, cacher, (req, res) => {
    let { search, location } = req.query;
    // TODO: santisation checks
    let url = "";
    if (!location) {
        url = `https://www.reed.co.uk/api/1.0/search?keywords=${search}&resultsToTake=15`;
    } else {
        url = `https://www.reed.co.uk/api/1.0/search?keywords=${search}&locationName=${location}&resultsToTake=15`;
    }

    let config = {
        method: "get",
        url: url,
        headers: {
            Authorization: process.env.REED_AUTH,
        },
    };

    axios(config)
        .then(function (response) {
            let responseData = response.data;

            let logoPromises = [];

            for (let i = 0; i < responseData.results.length; i++) {
                let empName = responseData.results[i]["employerName"];
                let logoConfig = {
                    method: "get",
                    url: `https://autocomplete.clearbit.com/v1/companies/suggest?query=${empName}`,
                };

                let thisPromise = axios(logoConfig);
                logoPromises.push(thisPromise);
            }

            Promise.all(logoPromises).then((responses) => {
                for (response in responses) {
                    if (responses[response].data[0]) {
                        responseData.results[response]["logoUrl"] =
                            responses[response].data[0].logo;
                        responseData.results[response]["extUrl"] =
                            responses[response].data[0].domain;
                    } else {
                        responseData.results[response]["logoUrl"] =
                            "https://i.imgur.com/uU0G6CL.png";
                        responseData.results[response]["extUrl"] =
                            "https://www.google.com/";
                    }
                }
                cache.setKey(req.originalUrl, {
                    date: moment(),
                    data: {
                        success: true,
                        jobs: responseData.results,
                    },
                });
                res.send({
                    success: true,
                    jobs: responseData.results,
                });
            });
        })
        .catch(function (error) {
            console.log(error);
        });
});

app.get("/api/moreDetails", blockNotAuthenticated, cacher, async (req, res) => {
    let { empName, empID, jobID } = req.query;

    if (empName == "" || empID == "" || jobID == "") {
        res.send({
            success: "false",
            msg:
                "request must include ?empName=employerName&empID=1234&jobID=5678",
        });
    }

    let moreDetailsReturn = {};

    // Financial data requests
    let symbolSearchConfig = {
        method: "get",
        url: `https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=${empName}&apikey=${process.env.ALPHA_AUTH}`,
        headers: {},
    };

    let summarisedFinanceData = [];
    const symbolSearchCall = axios(symbolSearchConfig).catch(function (error) {
        console.log(error);
    });

    // Reed more details request
    let jobDetailsConfig = {
        method: "get",
        url: `https://www.reed.co.uk/api/1.0/jobs/${jobID}`,
        headers: {
            Authorization: process.env.REED_AUTH,
        },
    };

    const jobDetailsCall = axios(jobDetailsConfig);

    const reviewDataCall = pool.query(
        `SELECT employer_id, user_id, rating, title, description
	FROM review_tbl
	WHERE employer_id=$1;`,
        [empID],
        (err, results) => {
            if (err) {
                throw err;
            }

            if (results.rows.length > 0) {
                moreDetailsReturn["reviewData"] = results.rows;
            } else {
                console.log(`review data not found for empID ${empID}`);
                moreDetailsReturn["reviewData"] = [];
            }
        }
    );

    Promise.all([symbolSearchCall, jobDetailsCall, reviewDataCall])
        .then((responses) => {
            // initialise phase 2 requests promises
            const promises = [];

            // handle jobDetails data response
            const jobDetailsResponse = responses[1];
            moreDetailsReturn["jobDetails"] = [jobDetailsResponse.data];

            let logoConfig = {
                method: "get",
                url: `https://autocomplete.clearbit.com/v1/companies/suggest?query=${empName}`,
            };

            let logoPromise = axios(logoConfig);
            promises.push(logoPromise);

            // handle reviewDataresponse
            const reviewResponse = responses[2];

            // handle finance data response
            const symbolResponse = responses[0];
            let symbol;
            if (symbolResponse.data.bestMatches[0]) {
                symbol = JSON.stringify(
                    symbolResponse.data.bestMatches[0]["1. symbol"]
                );
                symbol = symbol.replace(/['"]+/g, "");
            }

            if (symbol) {
                // financial data found, 2nd request start
                let fincancialDataConfig = {
                    method: "get",
                    url: `https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=${symbol}&apikey=${process.env.ALPHA_AUTH}`,
                    headers: {},
                };

                const financialDataPromise = axios(fincancialDataConfig);
                promises.push(financialDataPromise);
                // end of 2nd request
            } else {
                console.log("finance data not found for company of that name");
                moreDetailsReturn["financeData"] = [];
            }
            Promise.all(promises).then((responses) => {
                if (responses.length > 1) {
                    // finance data request was made
                    let financeData = responses[1].data;
                    financeData = financeData["Time Series (Daily)"];

                    for (let i = 0; i < Object.keys(financeData).length; i++) {
                        let thisDate = Object.keys(financeData)[i];
                        summarisedFinanceData.push({
                            date: thisDate,
                            sharePrice: financeData[thisDate]["4. close"],
                        });
                    }
                    moreDetailsReturn["financeData"] = summarisedFinanceData;
                    console.log("finance data found!");
                }

                let logoResponse = responses[0].data;

                if (logoResponse.length != 0) {
                    moreDetailsReturn["jobDetails"][0]["logoUrl"] =
                        logoResponse[0].logo;
                    moreDetailsReturn["jobDetails"][0]["extUrl"] =
                        logoResponse[0].domain;
                } else {
                    console.log("no logo found");
                }

                cache.setKey(req.originalUrl, {
                    date: moment(),
                    data: moreDetailsReturn,
                });
                res.send(moreDetailsReturn);
            });
        })
        .catch((errors) => {
            console.log(errors);
        });
});

app.get("/api/pinned", blockNotAuthenticated, (req, res) => {
    let userID = req.user.user_id;

    if (!userID) {
        res.send({
            success: "false",
            error: "please include user_id as param ?user=",
        });
    }

    pool.query(
        `SELECT job_id
	FROM pinned_tbl
	WHERE user_id=$1;`,
        [userID],
        (err, results) => {
            if (err) {
                throw err;
            }

            let pinnedResponse = [];

            if (results.rows.length > 0) {
                for (let i = 0; i < results.rows.length; i++) {
                    // Reed more details request
                    let jobDetailsConfig = {
                        method: "get",
                        url: `https://www.reed.co.uk/api/1.0/jobs/${results.rows[i].job_id}`,
                        headers: {
                            Authorization: process.env.REED_AUTH,
                        },
                    };

                    axios(jobDetailsConfig)
                        .then((response) => {
                            let thisJob = {};
                            thisJob["jobId"] = response.data["jobId"];
                            thisJob["employerId"] = response.data["employerId"];
                            thisJob["employerName"] =
                                response.data["employerName"];
                            thisJob["employerProfileId"] = "";
                            thisJob["employerProfileName"] = "";
                            thisJob["jobTitle"] = response.data["jobTitle"];
                            thisJob["locationName"] =
                                response.data["locationName"];
                            thisJob["minimumSalary"] =
                                response.data["minimumSalary"];
                            thisJob["maximumSalary"] =
                                response.data["maximumSalary"];
                            thisJob["currency"] = response.data["currency"];
                            thisJob["expirationDate"] =
                                response.data["expirationDate"];
                            thisJob["date"] = response.data["datePosted"];
                            thisJob["jobDescription"] = response.data[
                                "jobDescription"
                            ].replace(/(<([^>]+)>)/gi, "");
                            thisJob["applications"] = 0;
                            thisJob["jobUrl"] = response.data["jobUrl"];
                            thisJob["logoUrl"] =
                                "https://i.imgur.com/uU0G6CL.png";
                            thisJob["extUrl"] = "https://www.google.com/";
                            pinnedResponse.push(thisJob);
                            if (i == results.rows.length - 1) {
                                res.send({
                                    success: true,
                                    jobs: pinnedResponse,
                                });
                            }
                        })
                        .catch((err) => {
                            console.log(err);
                        });
                }
            } else {
                res.send({
                    success: false,
                    msg: "there are no pinned jobs for this user",
                });
            }
        }
    );
});

app.post("/api/pinned", blockNotAuthenticated, (req, res) => {
    let { jobID } = req.body;
    let userID = req.user.user_id;

    if (!userID || !jobID) {
        res.send({
            success: false,
            msg: "please include params userID and jobID",
        });
    } else {
        pool.query(
            `INSERT INTO pinned_tbl(
	job_id, user_id)
	VALUES ($1, $2) RETURNING job_id, user_id;`,
            [jobID, userID],
            (err, results) => {
                if (err) {
                    console.log(err);
                    res.send({
                        success: false,
                        msg: `error adding pinned job to database.  either post is already pinned to that user, or user with userID ${userID} does not exist in db`,
                    });
                } else {
                    res.send({
                        success: true,
                        msg: "Pin added to db",
                    });
                }
            }
        );
    }
});

app.delete("/api/pinned", blockNotAuthenticated, (req, res) => {
    let { jobID } = req.body;
    let userID = req.user.user_id;

    if (!jobID || !userID) {
        return res.send({
            success: false,
            msg: "Please include jobID and userID as params",
        });
    }

    pool.query(
        `SELECT job_id, user_id
	FROM public.pinned_tbl
	WHERE job_id=$1
	AND user_id=$2;`,
        [jobID, userID],
        (err, results) => {
            if (err) {
                throw err;
            }

            if (results.rows.length == 0) {
                return res.send({
                    success: false,
                    msg: `Pinned post with user id ${userID} and job id ${jobID} not found in db.`,
                });
            }

            pool.query(
                `DELETE FROM public.pinned_tbl
	            WHERE job_id=$1
	            AND user_id=$2;`,
                [jobID, userID],
                (err, results) => {
                    if (err) {
                        throw err;
                    }
                    res.send({
                        success: true,
                        msg: "post successfully un-pinned",
                    });
                }
            );
        }
    );
});

app.post("/api/register", blockAuthenticated, async (req, res) => {
    let {
        username,
        email,
        password,
        password2,
        firstName,
        lastName,
        dob,
    } = req.body;

    let errors = [];

    if (
        !username ||
        !email ||
        !password ||
        !password2 ||
        !firstName ||
        !lastName ||
        !dob
    ) {
        errors.push({
            msg: "1 or more fields left empty",
        });
    }

    if (password.length < 6) {
        errors.push({
            msg: "Password should be at least 6 characters",
        });
    }

    if (password != password2) {
        errors.push({
            msg: "Passwords do not match",
        });
    }

    if (errors.length > 0) {
        res.send(errors);
    } else {
        // validation passed
        let hashedPassword = await bcrypt.hash(password, 10);
        pool.query(
            `SELECT * FROM user_tbl WHERE email = $1`,
            [email],
            (err, results) => {
                if (err) {
                    throw err;
                }

                if (results.rows.length > 0) {
                    // user already registered
                    errors.push({
                        msg: "Email already registered",
                    });
                    res.send(errors);
                } else {
                    pool.query(
                        `INSERT INTO user_tbl (first_name, last_name, email, dob, password_hash, username) 
                        VALUES ($1, $2, $3, $4, $5, $6) 
                        RETURNING username, password_hash`,
                        [
                            firstName,
                            lastName,
                            email,
                            dob,
                            hashedPassword,
                            username,
                        ],
                        (err, results) => {
                            if (err) {
                                throw err;
                            }
                            res.send({
                                msg: `user created, with username ${username}`,
                            });
                        }
                    );
                }
            }
        );
    }
});

app.post("/api/review", blockNotAuthenticated, (req, res) => {
    let { empID, rating, title, desc } = req.body;
    let userID = req.user.user_id;

    if (!empID || !userID || !rating || !title) {
        return res.send({
            success: false,
            msg:
                "empID, userID, rating and title are all required in request body, desc is optional",
        });
    }

    pool.query(
        `SELECT employer_id, user_id, rating, title, description
	FROM review_tbl
	WHERE employer_id=$1
    AND user_id=$2`,
        [empID, userID],
        (err, results) => {
            if (err) {
                throw err;
            }
            if (results.rows.length != 0) {
                // user already left review for that employer
                return res.send({
                    success: false,
                    msg: "user already left review for that employer",
                });
            }

            if (desc) {
                pool.query(
                    `INSERT INTO review_tbl(
	        employer_id, user_id, rating, title, description)
	        VALUES ($1, $2, $3, $4, $5);`,
                    [empID, userID, rating, title, desc],
                    (err, results) => {
                        if (err) {
                            throw err;
                        }
                        return res.send({
                            success: true,
                            msg: "Review successfully added",
                        });
                    }
                );
            } else {
                pool.query(
                    `INSERT INTO review_tbl(
	        employer_id, user_id, rating, title)
	        VALUES ($1, $2, $3, $4);`,
                    [empID, userID, rating, title],
                    (err, results) => {
                        if (err) {
                            throw err;
                        }
                        return res.send({
                            success: true,
                            msg: "Review successfully added",
                        });
                    }
                );
            }
        }
    );
});

app.delete("/api/review", blockNotAuthenticated, (req, res) => {
    let { empID } = req.body;
    let userID = req.user.user_id;

    if (!empID || !userID) {
        return res.send({
            success: false,
            msg: "params empID and userID are required for this endpoint",
        });
    }

    pool.query(
        `SELECT employer_id, user_id, rating, title, description
	FROM review_tbl
	WHERE employer_id=$1
	AND user_id=$2;`,
        [empID, userID],
        (err, results) => {
            if (err) {
                throw err;
            }

            if (results.rows.length == 0) {
                return res.send({
                    success: false,
                    msg: `review not found for empID ${empID} and userID ${userID}`,
                });
            }

            pool.query(
                `DELETE FROM review_tbl
		        WHERE employer_id=$1
	            AND user_id=$2;`,
                [empID, userID],
                (err, results) => {
                    if (err) {
                        throw err;
                    }
                    return res.send({
                        success: true,
                        msg: "review successfully deleted",
                    });
                }
            );
        }
    );
});

app.get("/api/review", blockNotAuthenticated, (req, res) => {
    let { empID } = req.query;
    let userID = req.user.user_id;

    if (!empID || !userID) {
        return res.send({
            success: false,
            msg: "params empID and userID are required for this endpoint",
        });
    }

    pool.query(
        `SELECT employer_id, user_id, rating, title, description
	FROM review_tbl
	WHERE employer_id=$1
	AND user_id=$2;`,
        [empID, userID],
        (err, results) => {
            if (err) {
                throw err;
            }

            if (results.rows.length == 0) {
                return res.send({
                    success: false,
                    msg: `review not found for empID ${empID} and userID ${userID}`,
                });
            }
            res.send({
                success: true,
                review: results.rows,
            });
        }
    );
});

app.put("/api/review", blockNotAuthenticated, (req, res) => {
    let { rating, title, desc, empID } = req.body;
    let userID = req.user.user_id;

    if (!rating || !title || !desc || !empID || !userID) {
        return res.send({
            success: false,
            msg:
                "params rating, title, desc and empID are all required for this endpoint",
        });
    }

    pool.query(
        `SELECT employer_id, user_id, rating, title, description
	FROM review_tbl
	WHERE employer_id=$1
	AND user_id=$2;`,
        [empID, userID],
        (err, results) => {
            if (err) {
                throw err;
            }

            if (results.rows.length == 0) {
                return res.send({
                    success: false,
                    msg: `review not found for empID ${empID} and userID ${userID}`,
                });
            }

            pool.query(
                `UPDATE review_tbl
	            SET rating=$1, title=$2, description=$3
	            WHERE employer_id=$4
	            AND user_id=$5;`,
                [rating, title, desc, empID, userID],
                (err, results) => {
                    if (err) {
                        throw err;
                    }
                    return res.send({
                        success: true,
                        msg: "review successfully updated",
                    });
                }
            );
        }
    );
});

app.post(
    "/api/login",
    blockAuthenticated,
    passport.authenticate("local"),
    (req, res) => {
        // send userID to frontend
        pool.query(
            `SELECT user_id
	FROM user_tbl
	WHERE email=$1;`,
            [req.body.email],
            (err, results) => {
                if (err) {
                    throw err;
                }
                if (results.rows.length == 0) {
                    return res.send({
                        success: false,
                        msg:
                            "login worked, but cant find userID for that email",
                    });
                }
                res.send({
                    success: true,
                    msg: "logged in",
                    userID: results.rows[0]["user_id"],
                });
            }
        );
    }
);

app.get("/api/logout", blockNotAuthenticated, (req, res) => {
    req.logOut();
    res.send({
        success: true,
        msg: "logged out",
    });
});

app.get("/api/loginError", blockAuthenticated, (req, res) => {
    res.send({
        success: false,
        msg:
            "you need to be logged in to access this endpoint.  please log in or make an account",
    });
});

// this could be replaced by a more useful redirect, meaning the frontend can just call login
app.get("/api/logoutError", blockNotAuthenticated, (req, res) => {
    res.send({
        success: false,
        msg:
            "you do not need to access this endpoint as you are already logged in.",
    });
});

// Port assignment
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server started on port ${PORT}`));
