import React, { useEffect, useState } from 'react';

function Login(){

  // function handleClick() {
  //   console.log('Button Clicked!');
  //   // interact with the backend in order to determine whether or not the login in was successful
  //   // redirect to /auth
  // }

  return (
    <div className='center'>
      <h1 className="header">Twexter</h1>
      <div className='center'>
        <a href='/auth'>Log In!!</a>
      </div>
    </div>
  )
};

export default Login;