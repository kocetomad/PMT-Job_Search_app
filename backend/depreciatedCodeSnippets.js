app.post("/api/badLogin", (req, res) => {
    const { username, password } = req.body;
    console.log(`username = ${username}, password = ${password}`);
    if (username && password) {
        if (req.session.authenticated) {
            // TODO: regen session cookies
            res.json(req.session); // TEMP - remove.  dont send passworks back
        } else {
            if (username == "test" && password == "123") {
                // TEMP change me to db check.
                req.session.authenticated = true;
                req.session.user = { username, password };
                res.json(req.session); // TEMP - remove.  dont send passworks back
            } else {
                res.send("wrong username / password");
            }
        }
    } else {
        // res.send("username and password required");
        res.json(req.session); // TEMP - remove.  dont send passworks back
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

// ===============================================================================================

let allDetails = [];
for (let i = 0; i < 5; i++) {
    // update to suit
    let thisJobID = responseData["results"][i]["jobId"];
    console.log(thisJobID);

    let config2 = {
        method: "get",
        url: `https://www.reed.co.uk/api/1.0/jobs/${thisJobID}`,
        headers: {
            Authorization: process.env.REED_AUTH,
        },
    };

    axios(config2)
        .then(function (response) {
            let thisRD = response.data;
            console.log(`thisRD = ${thisRD}`);
            allDetails.push({ response: response.data });
            console.log(allDetails);
            if (allDetails.length > 4) {
                // needs to be done in promise or data will be send back before array is populated
                res.send(allDetails);
            }
        })
        .catch(function (error) {
            console.log(error);
        });
} // end of company logo request
