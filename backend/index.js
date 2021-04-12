// Imports
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

app.get("/api/job", (req, res) => {
    let searchTerm = req.query.search;
    res.send(searchTerm);
});

// Port assignment
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server started on port ${PORT}`));
