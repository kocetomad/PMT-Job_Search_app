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
const checkAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) {
        return res.redirect("/logoutError");
    }
    next();
};

const checkNotAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) {
        return next();
    }

    res.redirect("/loginError");
};

// Routes
app.get("/", checkNotAuthenticated, (req, res) => {
    res.send({ success: "true" });
});

app.get("/api/jobs", (req, res) => {
    // add auth check
    let { search, location } = req.query;
    // TODO: santisation checks
    let url = "";
    if (!location) {
        url = `https://www.reed.co.uk/api/1.0/search?keywords=${search}`;
    } else {
        url = `https://www.reed.co.uk/api/1.0/search?keywords=${search}&locationName=${location}`;
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
            // let responseData = response.data;
            let responseData = response.data;
            res.send(responseData.results);
            // company logo scraping functionality placeholder
        })
        .catch(function (error) {
            console.log(error);
        });
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
                console.log({
                    reviewStatus: true,
                    reviewData: results.rows,
                });
                // reviewData.push(results.rows);
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
                console.log(`symbol: ${symbol}`);
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

app.get("/api/pinned", checkNotAuthenticated, (req, res) => {
    let userID = req.query.user;
    if (userID) {
        res.send({
            success: "true",
            pinnedPosts: [
                { postPlaceholder1: "blah" },
                { postPlaceholder2: "blah again" },
            ],
            queryTest: [{ userID: userID }],
        });
    } else {
        res.send({
            success: "false",
            error: "please include user_id as param ?user=",
        });
    }
});

app.post("/api/register", checkAuthenticated, async (req, res) => {
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

app.post(
    "/login",
    checkAuthenticated,
    passport.authenticate("local", {
        successRedirect: "/",
    })
);

app.get("/logout", checkNotAuthenticated, (req, res) => {
    req.logOut();
    res.send({
        success: true,
        msg: "logged out",
    });
});

app.get("/loginError", checkAuthenticated, (req, res) => {
    res.send({
        success: false,
        msg:
            "you need to be logged in to access this endpoint.  please log in or make an account",
    });
});

// this could be replaced by a more useful redirect, meaning the frontend can just call login
app.get("/logoutError", checkNotAuthenticated, (req, res) => {
    res.send({
        success: false,
        msg:
            "you do not need to access this endpoint as you are already logged in.",
    });
});

// Port assignment
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server started on port ${PORT}`));
