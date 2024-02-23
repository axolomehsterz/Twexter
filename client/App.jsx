import React, { useEffect, useState } from "react";
import Feed from "./routes/Feed.jsx";
import Login from "./routes/Login.jsx";
import ErrorPage from "./routes/ErrorPage.jsx";
import "./styles.css";
import { Route, Routes, useSearchParams } from "react-router-dom";
import Footer from "./Footer.jsx";

import Logo from "./images/logo.png";
import Register from "./routes/Register.jsx";
function App() {
  const [text, setText] = useState();
  const [user, setUser] = useState();
  const [searchParams, setSearchParams] = useSearchParams();

  useEffect(() => {
    async function getData() {
      console.log('something')
      const result = await fetch("/api");
      const text = await result.text();
      setUser(searchParams.get("user"));
    }
    getData();
  }, []);

  return (
    // Specify Routes in Here
    <div className="App">
      <header>
        <div id="logo">
          <img src={Logo} />
        </div>
        <h1 className="font-semibold text-4xl">Welcome to Twexter!</h1>
        <div id="logo">
          <img src={Logo} />
        </div>
      </header>
      {user ? <h2>Welcome, {user}!</h2> : null}
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/feed" element={<Feed />} />
        <Route path="/error" element={<ErrorPage />} />
      </Routes>
      <Footer className=""></Footer>
      {/* <footer>
        Created by{' '}
        <a href="https://github.com/axolomehsterz">the axolomehsterz</a> 💪
      </footer> */}
      {/* <Link to="/error">Click me</Link> */}
    </div>
  );
}

export default App;
