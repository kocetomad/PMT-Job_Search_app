// Imports
require("dotenv").config();
const express = require("express");
const path = require("path");
const axios = require("axios").default;
const fs = require("fs");
const session = require("express-session");
const { pool } = require("./dbConfig");
const bcrypt = require("bcrypt");

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

// Routes
app.get("/", (req, res) => {
    res.send(JSON.stringify({ ok: "true" }));
});

app.get("/api/jobs", (req, res) => {
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
            // let responseData = JSON.stringify(response.data);
            let responseData = response.data;
            res.send(responseData);
            // company logo scraping functionality placeholder
        })
        .catch(function (error) {
            console.log(error);
        });
});

app.get("/api/moreDetails", (req, res) => {
    // console.log(JSON.stringify({ financeStatus: true, financeData: financeData }));
    let empName = req.query.empName;
    let empID = req.query.empID;

    if (empName == "" || empID == "") {
        res.send(
            JSON.stringify({
                ok: "false",
                message:
                    "request must include ?empName=employerName&empID=1234",
            })
        );
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

app.get("/api/pinned", (req, res) => {
    let userID = req.query.user;
    if (userID) {
        res.send(
            JSON.stringify({
                ok: "true",
                pinnedPosts: [
                    { postPlaceholder1: "blah" },
                    { postPlaceholder2: "blah again" },
                ],
                queryTest: [{ userID: userID }],
            })
        );
    } else {
        res.send(
            JSON.stringify({
                ok: "false",
                error: "please include user_id as param ?user=",
            })
        );
    }
});

app.post("/api/register", async (req, res) => {
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

// temp routes for development
app.get("/dev/resetSessions", (req, res) => {
    req.session.authenticated = false;
    res.send("sessions reset");
});

app.get("/dev/testSessions", (req, res) => {
    if (req.session.authenticated) {
        res.send("testing login only data");
    } else {
        res.send("error, user not logged in");
    }
});

// Port assignment
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server started on port ${PORT}`));
