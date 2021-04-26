const LocalStrategy = require("passport-local").Strategy;
const { pool } = require("./dbConfig");
const bcrypt = require("bcrypt");

const initialize = (passport) => {
    const authenticateUser = (email, password, done) => {
        pool.query(
            `SELECT * FROM user_tbl WHERE email=$1`,
            [email],
            (err, results) => {
                if (err) {
                    throw err;
                }
                console.log(results.rows);
                if (results.rows.length > 0) {
                    // user found
                    const user = results.rows[0];

                    bcrypt.compare(
                        password,
                        user.password_hash, // unsure
                        (err, isMatch) => {
                            if (err) {
                                throw err;
                            }
                            if (isMatch) {
                                return done(null, user);
                            } else {
                                return done(null, false, {
                                    msg: "password is incorrect",
                                });
                            }
                        }
                    );
                } else {
                    // user not found
                    return done(null, false, { msg: "email not registered" });
                }
            }
        );
    };

    passport.use(
        new LocalStrategy(
            {
                usernameField: "email",
                passwordField: "password", // unsure
            },
            authenticateUser
        )
    );

    passport.serializeUser((user, done) => {
        // allows db data to be matched with cookie
        done(null, user.user_id);
        // stores user ID in session
    });

    passport.deserializeUser((user_id, done) => {
        // allows cookie data to be matched with db
        pool.query(
            `SELECT * FROM user_tbl WHERE user_id=$1`,
            [user_id],
            (err, results) => {
                if (err) {
                    throw err;
                }
                return done(null, results.rows[0]);
            }
        );
    });
};

module.exports = initialize;
