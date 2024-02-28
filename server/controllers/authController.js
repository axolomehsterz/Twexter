const dotenv = require("dotenv");
const { makeMWareBanner } = require("../utils.js");
const authController = {};
const db = require("../models/twexterModel.js");
const bcrypt = require("bcrypt");

const mwareBanner = makeMWareBanner("authController");

authController.getGithubToken = async ({ query: { code } }, res, next) => {
  mwareBanner("getGithubToken");
  // console.log(code);

  try {
    const fromGithub = await fetch(
      "https://github.com/login/oauth/access_token",
      {
        method: "POST",
        body: JSON.stringify({
          client_id: process.env.GITHUB_CLIENT_ID,
          client_secret: process.env.GITHUB_SECRET,
          code,
        }),
        headers: {
          "Content-Type": "application/json",
        },
      },
    );

    const token = await fromGithub.text();
    res.locals.token = token;
    return next();
  } catch (e) {
    return next({
      log: "Error: failure in authController.getGithubToken -- " + e,
      status: 400,
      message: { err: "Was not able to get token from GitHub" },
    });
  }
};
authController.getGithubUsername = async (req, res, next) => {
  mwareBanner("getGithubUsername");

  const { token } = res.locals;
  console.log("Bearer " + token.split("&")[0].split("=")[1]);
  try {
    const fromGithub = await fetch("https://api.github.com/user", {
      type: "POST",
      headers: {
        Authorization: "Bearer " + token.split("&")[0].split("=")[1],
      },
    });

    const githubUser = await fromGithub.json();
    console.log("Found user: " + githubUser);

    // res.cookie('twexter', githubUser.login, { httpOnly: true });
    res.locals.username = githubUser.login;
    return next();
  } catch (e) {
    return next({
      log: "Error: failure in authController.getGithubUsername -- " + e,
      status: 400,
      message: { err: "Failed to retrieve username from Github" },
    });
  }
};

authController.register = async (req, res, next) => {
  try {
    const body = req.body;
    const newUsername = body.username;
    const newPassword = body.password;
    if (!newUsername) {
      ('"newUsername" is missing from the request body');
    } else if (!newPassword) {
      throw '"password" is missing from the request body';
    }
    //Check if that username already exists
    let query = `SELECT * FROM users WHERE username = $1`;
    let result = await db.query(query, [newUsername]);
    console.log("result", result);
    let response = result.rows[0];
    //response will be null if nothing is returned from the database
    console.log("response", response);
    if (response) {
      throw "A user with that name already exists";
    }
    const saltRounds = 10;
    bcrypt.genSalt(saltRounds, (err, salt) => {
      bcrypt.hash(newPassword, salt, async (err, hashedPassword) => {
        if (err) {
          throw err;
        }
        query = `INSERT INTO users (username, password) VALUES ($1, $2);`;
        result = await db.query(query, [newUsername, hashedPassword]);
      });
    });
    return next();
  } catch (err) {
    next(err);
  }
};

module.exports = authController;
