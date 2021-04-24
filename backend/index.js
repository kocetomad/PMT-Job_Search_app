// Imports
require("dotenv").config();
const express = require("express");
const path = require("path");
const axios = require("axios").default;
const fs = require("fs");

// Initialisation
const app = express();

const logger = (req, res, next) => {
    console.log(
        `${req.method}  -  ${req.protocol}://${req.get("host")}${
            req.originalUrl
        }`
    );
    next();
};

// Middleware
app.use(express.json());
app.use(logger);

// Routes
app.get("/", (req, res) => {
    // Temp json experiments (reduce no. of api queries I send during testing)
    let financeData = JSON.parse(fs.readFileSync("sample-finance-data.json"));
    financeData = financeData["Time Series (Daily)"];
    let summarisedFinanceData = [];
    for (let i = 0; i < Object.keys(financeData).length; i++) {
        summarisedFinanceData.push({
            date: Object.keys(financeData)[i],
            sharePrice: financeData["2020-11-27"]["4. close"],
        });
    }
    let jsonTesting = summarisedFinanceData;
    res.send(jsonTesting);
    // console.log(JSON.stringify({ financeStatus: true, financeData: financeData }));

    // res.send(JSON.stringify({ ok: "true" }));
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
            let responseData = JSON.stringify(response.data);
            res.send(responseData);
        })
        .catch(function (error) {
            console.log(error);
        });
});

app.get("/api/moreDetails", (req, res) => {
    let empName = req.query.empName;
    let empID = req.query.empID;

    if (empName == "" || empID == "") {
        res.send(
            JSON.stringify({
                ok: "false",
                message:
                    "request must include ?empName=employerName&empID=5678",
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
                        let financeData = JSON.stringify(response.data);
                        res.send(financeData);
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

app.post("/api/postTest", (req, res) => {
    res.send(JSON.stringify(req.body));
});

// Port assignment
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server started on port ${PORT}`));
