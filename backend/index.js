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
    console.log(
        `${req.method}  -  ${req.protocol}://${req.get("host")}${
            req.originalUrl
        }`
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
    res.send({ ok: "true" });
});

app.get("/api/jobs", checkNotAuthenticated, (req, res) => {
    let searchTerm = req.query.search;
    let location = req.query.location;
    // TODO: santisation checks
    let url = `https://www.reed.co.uk/api/1.0/search?keywords=${searchTerm}&locationName=${location}`;

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
            res.send(responseData);
            // company logo scraping functionality placeholder
        })
        .catch(function (error) {
            console.log(error);
        });
});

app.get("/api/moreDetails", checkNotAuthenticated, (req, res) => {
    // console.log({ financeStatus: true, financeData: financeData });
    let empName = req.query.empName;
    let empID = req.query.empID;

    if (empName == "" || empID == "") {
        res.send({
            ok: "false",
            message: "request must include ?empName=employerName&empID=1234",
        });
    }

    let shortName = empName.split(" ")[0]; // sometimes gives better results in request, sometimes doesn't

    let config = {
        method: "get",
        url: `https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=${empName}&apikey=${process.env.ALPHA_AUTH}`,
        headers: {},
    };

    axios(config)
        .then(function (response) {
            let symbol = JSON.stringify(
                response.data.bestMatches[0]["1. symbol"]
            );
            symbol = symbol.replace(/['"]+/g, "");
            if (symbol != "") {
                // financial data found, 2nd request start
                let config2 = {
                    method: "get",
                    url: `https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=${symbol}&apikey=${process.env.ALPHA_AUTH}`,
                    headers: {},
                };

                axios(config2)
                    .then(function (response) {
                        let financeData = response.data;
                        financeData = financeData["Time Series (Daily)"];
                        let summarisedFinanceData = [];
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
                        res.send(summarisedFinanceData);
                    })
                    .catch(function (error) {
                        console.log(error);
                    });
                // end of 2nd request
            }
        })
        .catch(function (error) {
            console.log(error);
        });
});

app.get("/api/pinned", checkNotAuthenticated, (req, res) => {
    let userID = req.query.user;
    if (userID) {
        res.send({
            ok: "true",
            pinnedPosts: [
                { postPlaceholder1: "blah" },
                { postPlaceholder2: "blah again" },
            ],
            queryTest: [{ userID: userID }],
        });
    } else {
        res.send({
            ok: "false",
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
