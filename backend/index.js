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

// temp imports
let jobsPrefetch = require("./sample_reed_job_response.json");

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

app.get("/api/jobs", blockNotAuthenticated, (req, res) => {
    // // add auth check
    // let { search, location } = req.query;
    // // TODO: santisation checks
    // let url = "";
    // if (!location) {
    //     url = `https://www.reed.co.uk/api/1.0/search?keywords=${search}`;
    // } else {
    //     url = `https://www.reed.co.uk/api/1.0/search?keywords=${search}&locationName=${location}`;
    // }

    // let config = {
    //     method: "get",
    //     url: url,
    //     headers: {
    //         Authorization: process.env.REED_AUTH,
    //     },
    // };

    // axios(config)
    //     .then(function (response) {
    //         // let responseData = response.data;
    //         let responseData = response.data;
    //         res.send(responseData.results);
    //         // company logo scraping functionality placeholder
    //     })
    //     .catch(function (error) {
    //         console.log(error);
    //     });
    res.send(jobsPrefetch);
});

app.get("/api/moreDetails", async (req, res) => {
    let { empName, empID, jobID } = req.query;

    if (empName == "" || empID == "" || jobID == "") {
        res.send({
            success: "false",
            message:
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
            }
        }
    );

    Promise.all([symbolSearchCall, jobDetailsCall, reviewDataCall])
        .then((responses) => {
            // let moreDetailsReturn = {}

            // handle jobDetails data response
            const jobDetailsResponse = responses[1];
            moreDetailsReturn["jobDetails"] = [jobDetailsResponse.data];

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

                axios(fincancialDataConfig)
                    .then(function (response) {
                        let financeData = response.data;
                        financeData = financeData["Time Series (Daily)"];

                        for (
                            let i = 0;
                            i < Object.keys(financeData).length;
                            i++
                        ) {
                            let thisDate = Object.keys(financeData)[i];
                            summarisedFinanceData.push({
                                date: thisDate,
                                sharePrice: financeData[thisDate]["4. close"],
                            });
                        }
                        // res.send(summarisedFinanceData);
                        moreDetailsReturn[
                            "financeData"
                        ] = summarisedFinanceData;
                        console.log("finance data found!");
                        res.send(moreDetailsReturn);
                    })
                    .catch(function (error) {
                        console.log(error);
                    });
                // end of 2nd request
            } else {
                // no financial data found, send without it.
                console.log("finance data not found for company of that name");
                res.send(moreDetailsReturn);
            }
        })
        .catch((errors) => {
            console.log(errors);
        });
});

app.get("/api/pinned", (req, res) => {
    let userID = req.query.user;

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
                        pinnedResponse.push(response.data);
                        if (i == results.rows.length - 1) {
                            res.send(pinnedResponse);
                        }
                    })
                    .catch((err) => {
                        console.log(err);
                    });
            }
        }
    );
});

app.post("/api/pinned", (req, res) => {
    let { userID, jobID } = req.body;

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

app.delete("/api/pinned", (req, res) => {
    let { jobID, userID } = req.body;

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

app.post("/api/review", (req, res) => {
    let { empID, userID, rating, title, desc } = req.body;
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

app.delete("/api/review", (req, res) => {
    let { empID, userID } = req.body;

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

app.get("/api/review", (req, res) => {
    let { empID, userID } = req.query;

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

app.put("/api/review", (req, res) => {
    let { rating, title, desc, empID, userID } = req.body;

    if (!rating || !title || !desc || !empID || !userID) {
        return res.send({
            success: false,
            msg:
                "params rating, title, desc, empID and userID are all required for this endpoint",
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
    passport.authenticate("local", {
        successRedirect: "/",
    })
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

// testing 2
