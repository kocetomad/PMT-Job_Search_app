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
const {
    body,
    validationResult,
    check,
    sanitize,
} = require("express-validator");

// Initialisation
initializePassport(passport);
const app = express();
const placeholderFinanceData = JSON.parse(
    fs.readFileSync("placeholderFinanceData.json")
);
const placeholderReviewData = JSON.parse(
    fs.readFileSync("placeholderReviewData.json")
);

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
    return moment(date).isAfter(now.subtract(2, "h")); // temp increased to 2
};

const cacher = (req, res, next) => {
    let thisCache = cache.getKey(req.originalUrl);
    if (thisCache != null) {
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

// cached joblist style details requests
let getDetails = async (jobID) => {
    let cacheKey = `/api/hidden/details/${jobID}`;
    let currCache = cache.getKey(cacheKey);

    if (currCache != null) {
        return currCache;
    }

    let jobDetailsConfig = {
        method: "get",
        url: `https://www.reed.co.uk/api/1.0/jobs/${jobID}`,
        headers: {
            Authorization: process.env.REED_AUTH,
        },
    };

    try {
        const detailsResponse = await axios(jobDetailsConfig);
        cache.setKey(cacheKey, detailsResponse);
        return new Promise((resolve, reject) => {
            resolve(detailsResponse);
        });
    } catch (e) {
        return console.log(e);
    }
};

// Cached logo lookup
let getLogo = async (searchTerm) => {
    let thisCache = cache.getKey(`/api/hidden/logo/${searchTerm}`);
    if (thisCache != null) {
        return thisCache;
    }

    let logoConfig = {
        method: "get",
        url: `https://autocomplete.clearbit.com/v1/companies/suggest?query=${searchTerm}`,
    };

    try {
        let logoResponse = await axios(logoConfig);
        cache.setKey(`/api/hidden/logo/${searchTerm}`, logoResponse);
        return new Promise((resolve, reject) => {
            resolve(logoResponse);
        });
    } catch (e) {
        return console.log(e);
    }
};

// Sanitisation
let removeHTML = (strIn) => {
    strIn = strIn.replace(/<.*.*<\/.*>/, "");

    // add spaces after period if no space is present.  deliberately breaks links.
    strIn = strIn.replace(/(?<=[.,])(?=[^\s])/, " ");

    return strIn;
};

// Routes
app.get("/", (req, res) => {
    res.send({ success: "true" });
});

app.get(
    "/api/jobs",
    blockNotAuthenticated,

    check("search")
        .optional()
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("location")
        .optional()
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("distance").optional().toInt(),

    (req, res) => {
        let { search, location, partTime, fullTime, distance } = req.query;
        let url = `https://www.reed.co.uk/api/1.0/search?keywords=${search}&resultsToTake=15`;
        if (location) {
            url += `&locationName=${location}`;
        }
        if (partTime) {
            if (partTime != "true" && partTime != "false") {
                console.log("partTime sanitation failed");
            } else {
                url += `&partTime=${partTime}`;
            }
        }
        if (fullTime) {
            if (fullTime != "true" && fullTime != "false") {
                console.log("fullTime sanitation failed");
            } else {
                url += `&fullTime=${fullTime}`;
            }
        }
        if (distance) {
            url += `&distanceFromLocation=${distance}`;
        }

        let thisCache = cache.getKey(url);
        if (thisCache) {
            if (!lessThanOneHourAgo(thisCache.date)) {
                console.log("cache too old, refetching");
            } else {
                console.log("cache worked");
                return res.send(thisCache.data);
            }
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
                    let thisPromise = getLogo(
                        responseData.results[i]["employerName"]
                    );
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
                            responseData.results[response]["logoUrl"] = "";
                            responseData.results[response]["extUrl"] =
                                "https://www.google.com/";
                        }
                    }
                    cache.setKey(url, {
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
    }
);

app.get(
    "/api/moreDetails",
    blockNotAuthenticated,

    check("empName")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),
    check("jobID").toInt(),
    check("empID").toInt(),

    async (req, res) => {
        let { empName, empID, jobID } = req.query;

        if (
            empName == "" ||
            !empName ||
            empID == "" ||
            !empID ||
            jobID == "" ||
            !jobID
        ) {
            return res.send({
                success: "false",
                msg: "request must include ?empName=employerName&empID=1234&jobID=5678",
            });
        }

        let thisCache = cache.getKey(
            `/api/hidden/moreDetails/${empName}/${empID}/${jobID}`
        );

        if (thisCache) {
            if (!lessThanOneHourAgo(thisCache.date)) {
                console.log("cache too old, refetching");
            } else {
                return res.send(thisCache.data);
            }
        }

        let moreDetailsReturn = {};

        // Financial data requests
        let symbolSearchConfig = {
            method: "get",
            url: `https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=${empName}&apikey=${process.env.ALPHA_AUTH}`,
            headers: {},
        };

        let summarisedFinanceData = [];
        const symbolSearchCall = axios(symbolSearchConfig).catch(function (
            error
        ) {
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
                    moreDetailsReturn["reviewData"] = placeholderReviewData;
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
                    console.log(
                        "finance data not found for company of that name"
                    );
                    moreDetailsReturn["financeData"] = [];
                }
                Promise.all(promises).then((responses) => {
                    if (responses.length > 1) {
                        // finance data request was made
                        let financeData = responses[1].data;
                        financeData = financeData["Time Series (Daily)"];

                        if (financeData) {
                            // checks for AV api rate limiting
                            for (
                                let i = 0;
                                i < Object.keys(financeData).length;
                                i++
                            ) {
                                let thisDate = Object.keys(financeData)[i];
                                summarisedFinanceData.push({
                                    date: thisDate,
                                    sharePrice:
                                        financeData[thisDate]["4. close"],
                                });
                            }
                            moreDetailsReturn["financeData"] =
                                summarisedFinanceData;
                            console.log("finance data found!");
                        } else {
                            console.log(
                                "AV Rate Limit Hit.  Finance data was found but cannot be displayed."
                            );
                            moreDetailsReturn["financeData"] =
                                placeholderFinanceData;
                        }
                    } else {
                        moreDetailsReturn["financeData"] =
                            placeholderFinanceData;
                    }

                    let logoResponse = responses[0].data;

                    if (logoResponse.length != 0) {
                        moreDetailsReturn["jobDetails"][0]["logoUrl"] =
                            logoResponse[0].logo;
                        moreDetailsReturn["jobDetails"][0]["extUrl"] =
                            logoResponse[0].domain;
                    } else {
                        moreDetailsReturn["jobDetails"][0]["logoUrl"] = "";
                        moreDetailsReturn["jobDetails"][0]["extUrl"] =
                            "https://www.google.com/";
                    }

                    cache.setKey(
                        `/api/hidden/moreDetails/${empName}/${empID}/${jobID}`,
                        {
                            date: moment(),
                            data: moreDetailsReturn,
                        }
                    );
                    res.send(moreDetailsReturn);
                });
            })
            .catch((errors) => {
                console.log(errors);
            });
    }
);

app.get("/api/pinned", blockNotAuthenticated, async (req, res) => {
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
                let reedDetailsPromises = [];
                for (let i = 0; i < results.rows.length; i++) {
                    thisResponse = getDetails(results.rows[i].job_id);
                    reedDetailsPromises.push(thisResponse);
                }

                Promise.all(reedDetailsPromises).then(async (responses) => {
                    for (response in responses) {
                        let thisJobLogo = await getLogo(
                            responses[response].data["employerName"]
                        );
                        let thisJob = {};
                        thisJob["jobId"] = responses[response].data["jobId"];
                        thisJob["employerId"] =
                            responses[response].data["employerId"];
                        thisJob["employerName"] =
                            responses[response].data["employerName"];
                        thisJob["employerProfileId"] = 0;
                        thisJob["employerProfileName"] = "";
                        thisJob["jobTitle"] =
                            responses[response].data["jobTitle"];
                        thisJob["locationName"] =
                            responses[response].data["locationName"];
                        thisJob["minimumSalary"] =
                            responses[response].data["minimumSalary"];
                        thisJob["maximumSalary"] =
                            responses[response].data["maximumSalary"];
                        thisJob["currency"] =
                            responses[response].data["currency"];
                        thisJob["expirationDate"] =
                            responses[response].data["expirationDate"];
                        thisJob["date"] =
                            responses[response].data["datePosted"];
                        thisJob["jobDescription"] = responses[response].data[
                            "jobDescription"
                        ].replace(/(<([^>]+)>)/gi, "");
                        thisJob["applications"] = 0;
                        thisJob["jobUrl"] = responses[response].data["jobUrl"];
                        if (
                            thisJobLogo.data != null &&
                            thisJobLogo.data != "" &&
                            thisJobLogo.data != []
                        ) {
                            thisJob["logoUrl"] = thisJobLogo.data[0].logo;
                        } else {
                            thisJob["logoUrl"] =
                                "https://i.imgur.com/uU0G6CL.png";
                        }
                        thisJob["extUrl"] = "https://www.google.com/";
                        pinnedResponse.push(thisJob);
                    }
                    return res.send({
                        success: true,
                        jobs: pinnedResponse,
                    });
                });
            } else {
                res.send({
                    success: false,
                    msg: "there are no pinned jobs for this user",
                });
            }
        }
    );
});

app.post(
    "/api/pinned",
    blockNotAuthenticated,
    check("jobID").toInt(),
    (req, res) => {
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
    }
);

app.delete(
    "/api/pinned",
    blockNotAuthenticated,
    check("jobID").toInt(),
    (req, res) => {
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
    }
);

app.post(
    "/api/register",
    blockAuthenticated,

    check("username")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("email").isEmail().normalizeEmail(),

    check("password")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("password2")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("firstName")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("lastName")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("dob")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    async (req, res) => {
        let {
            username,
            email,
            password,
            password2,
            firstName,
            lastName,
            dob,
            profilePic,
        } = req.body;

        // dob validation
        let dobDate = Date.parse(dob);

        if (isNaN(dobDate)) {
            return res.send({
                success: false,
                msg: "date in wrong format",
            });
        }

        let momentDob = moment(dob, "YYYY-MM-DD");

        if (momentDob.isAfter(moment().subtract(16, "years"))) {
            return res.send({
                success: false,
                msg: "You need to be 16 or older to register with this app.",
            });
        }

        // profilePic validation
        if (profilePic) {
            profilePic = removeHTML(profilePic);
            if (!/d*\.jpg$|\.jpeg$|\.png$|\.gif$|\.bmp$/.test(profilePic)) {
                return res.send({
                    success: false,
                    msg: "profilePic is not image link",
                });
            }
        }

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
            return res.send({
                success: false,
                errors: errors,
            });
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
                        return res.send({
                            success: false,
                            errors: errors,
                        });
                    }

                    pool.query(
                        `SELECT * FROM user_tbl WHERE username = $1`,
                        [username],
                        (err, results) => {
                            if (err) {
                                throw err;
                            }

                            if (results.rows.length > 0) {
                                return res.send({
                                    success: false,
                                    msg: "username already taken",
                                });
                            }

                            if (!profilePic) {
                                profilePic =
                                    "https://image.flaticon.com/icons/png/128/1946/1946429.png";
                            }
                            pool.query(
                                `INSERT INTO user_tbl (first_name, last_name, email, dob, password_hash, username, profile_url) 
                        VALUES ($1, $2, $3, $4, $5, $6, $7) 
                        RETURNING username, password_hash`,
                                [
                                    firstName,
                                    lastName,
                                    email,
                                    dob,
                                    hashedPassword,
                                    username,
                                    profilePic,
                                ],
                                (err, results) => {
                                    if (err) {
                                        throw err;
                                    }
                                    res.send({
                                        success: true,
                                        msg: `user created, with username ${username}`,
                                    });
                                }
                            );
                        }
                    );
                }
            );
        }
    }
);

app.post(
    "/api/review",
    blockNotAuthenticated,

    check("empID").toInt(),

    check("rating").toInt(),

    check("title")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("desc")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    (req, res) => {
        let { empID, rating, title, desc } = req.body;
        let userID = req.user.user_id;

        if (!empID || !userID || !rating || !title) {
            return res.send({
                success: false,
                msg: "empID, userID, rating and title are all required in request body, desc is optional",
            });
        }

        if (!(rating > 0 && rating < 6)) {
            return res.send({
                success: false,
                msg: "rating needs to be between 1 and 5 inclusive",
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
                            let allCache = Object.keys(cache.all());
                            for (cacheObject in allCache) {
                                if (
                                    String(allCache[cacheObject]).includes(
                                        empID
                                    )
                                ) {
                                    cache.removeKey(allCache[cacheObject]);
                                }
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
                            let allCache = Object.keys(cache.all());
                            for (cacheObject in allCache) {
                                if (
                                    String(allCache[cacheObject]).includes(
                                        empID
                                    )
                                ) {
                                    cache.removeKey(allCache[cacheObject]);
                                }
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
    }
);

app.delete(
    "/api/review",
    blockNotAuthenticated,
    check("empID").toInt(),
    (req, res) => {
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
                        let allCache = Object.keys(cache.all());
                        for (cacheObject in allCache) {
                            if (String(allCache[cacheObject]).includes(empID)) {
                                cache.removeKey(allCache[cacheObject]);
                            }
                        }
                        return res.send({
                            success: true,
                            msg: "review successfully deleted",
                        });
                    }
                );
            }
        );
    }
);

app.get(
    "/api/review",
    blockNotAuthenticated,
    check("empID").toInt(),
    (req, res) => {
        let { empID } = req.query;
        let userID = req.user.user_id;

        if (!empID || !userID) {
            return res.send({
                success: false,
                msg: "param empID is required for this endpoint",
            });
        }

        let thisCache = cache.getKey(`/api/hidden/review/${empID}/${userID}`);

        if (thisCache) {
            if (!lessThanOneHourAgo(thisCache.date)) {
                console.log("cache too old, refetching");
            } else {
                return res.send(thisCache.data);
            }
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
                    cache.setKey(`/api/hidden/review/${empID}/${userID}`, {
                        date: moment(),
                        data: {
                            success: false,
                            msg: `review not found for empID ${empID} and userID ${userID}`,
                        },
                    });
                    return res.send({
                        success: false,
                        msg: `review not found for empID ${empID} and userID ${userID}`,
                    });
                }
                cache.setKey(`/api/hidden/review/${empID}/${userID}`, {
                    date: moment(),
                    data: {
                        success: true,
                        review: results.rows,
                    },
                });
                res.send({
                    success: true,
                    review: results.rows,
                });
            }
        );
    }
);

app.put(
    "/api/review",
    blockNotAuthenticated,

    check("empID").toInt(),

    check("rating").toInt(),

    check("title")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    check("desc")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    (req, res) => {
        let { rating, title, desc, empID } = req.body;
        let userID = req.user.user_id;

        if (!rating || !title || !desc || !empID || !userID) {
            return res.send({
                success: false,
                msg: "params rating, title, desc and empID are all required for this endpoint",
            });
        }

        if (!(rating > 0 && rating < 6)) {
            return res.send({
                success: false,
                msg: "rating needs to be between 1 and 5 inclusive",
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
                        let allCache = Object.keys(cache.all());
                        for (cacheObject in allCache) {
                            if (String(allCache[cacheObject]).includes(empID)) {
                                cache.removeKey(allCache[cacheObject]);
                            }
                        }
                        return res.send({
                            success: true,
                            msg: "review successfully updated",
                        });
                    }
                );
            }
        );
    }
);

app.get("/api/profile", blockNotAuthenticated, (req, res) => {
    let userID = req.user.user_id;

    pool.query(
        `SELECT first_name, last_name, email, dob, username, profile_url
	FROM user_tbl 
	WHERE user_id=$1;`,
        [userID],
        (err, results) => {
            if (err) {
                throw err;
            }
            let profileReturn = {
                success: true,
                profile: results.rows,
            };
            if (
                profileReturn.profile[0]["profile_url"] == "" ||
                !profileReturn.profile[0]["profile_url"]
            ) {
                profileReturn.profile[0]["profile_url"] =
                    "https://image.flaticon.com/icons/png/128/1946/1946429.png";
            }
            res.send(profileReturn);
        }
    );
});

app.delete("/api/profile", blockNotAuthenticated, (req, res) => {
    let userID = req.user.user_id;

    pool.query(
        `DELETE FROM public.user_tbl
	WHERE user_id=$1;`,
        [userID],
        (err, results) => {
            if (err) {
                throw err;
            }
            req.logOut();
            res.send({
                success: true,
                msg: "profile successfully deleted",
            });
        }
    );
});

app.post(
    "/api/login",
    blockAuthenticated,

    check("email").isEmail().normalizeEmail(),

    check("password")
        .customSanitizer((value) => removeHTML(value))
        .trim()
        .escape(),

    passport.authenticate("local"),
    (req, res) => {
        // send userID to frontend
        pool.query(
            `SELECT user_id, profile_url
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
                        msg: "login worked, but cant find userID for that email",
                    });
                }
                let profileImage =
                    "https://image.flaticon.com/icons/png/128/1946/1946429.png";
                if (results.rows[0]["profile_url"]) {
                    profileImage = results.rows[0]["profile_url"];
                }
                res.send({
                    success: true,
                    msg: "logged in",
                    userID: results.rows[0]["user_id"],
                    profilePic: profileImage,
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
        msg: "you need to be logged in to access this endpoint.  please log in or make an account",
    });
});

app.get("/api/logoutError", blockNotAuthenticated, (req, res) => {
    res.send({
        success: false,
        msg: "you do not need to access this endpoint as you are already logged in.",
    });
});

// Port assignment
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server started on port ${PORT}`));
