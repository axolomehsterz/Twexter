import React, { useState } from "react";
import { Link } from "react-router-dom";

function Login() {
  async function handleSubmit() {
    const requestBody = {
      username: username,
      password: password,
    };
    console.log('butts')
    const response = await fetch("/auth/login", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });
    const json = await response.json();
    if (response.status === 200) {
      window.location.href = `/feed?user=${json.username}`;
    }
  }

  const [username, setUsername] = useState();
  const [password, setPassword] = useState();
  return (
    <div className="center">
      <h1 className="header">Twexter</h1>
      <div className="center">
        <a href="/auth">Log In with GitHub</a>
        <br />
        <a href="/auth/google">Log In with Google</a>
        <br />
        <br />
        <label>Username</label>
        <input
          className="form-control"
          type="username"
          id="username"
          name="password"
          onChange={(e) => setUsername(e.target.value)}
        />
        <label>Password</label>
        <input
          className="form-control"
          type="password"
          id="password"
          name="password"
          onChange={(e) => setPassword(e.target.value)}
        />
        <button type="submit" value="Submit" onClick={handleSubmit}>
          Submit
        </button>
        <br />
        <br />
        <Link to="/register">Register</Link>
      </div>
    </div>
  );
}

export default Login;
