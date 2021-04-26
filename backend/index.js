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

            // // company logo scraping goes here
            // let allDetails = [];
            // for (let i = 0; i < 5; i++) {
            //     // update to suit
            //     let thisJobID = responseData["results"][i]["jobId"];
            //     console.log(thisJobID);

            //     let config2 = {
            //         method: "get",
            //         url: `https://www.reed.co.uk/api/1.0/jobs/${thisJobID}`,
            //         headers: {
            //             Authorization: process.env.REED_AUTH,
            //         },
            //     };

            //     axios(config2)
            //         .then(function (response) {
            //             let thisRD = response.data;
            //             console.log(`thisRD = ${thisRD}`);
            //             allDetails.push({ response: response.data });
            //             console.log(allDetails);
            //             if (allDetails.length > 4) { // needs to be done in promise or data will be send back before array is populated
            //                 res.send(allDetails);
            //             }
            //         })
            //         .catch(function (error) {
            //             console.log(error);
            //         });
            // } // end of company logo request
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

app.post("/api/postTest", (req, res) => {
    res.send(JSON.stringify(req.body));
});

// Port assignment
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server started on port ${PORT}`));
