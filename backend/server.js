const express = require('express');
const axios = require('axios');
const bodyParser = require('body-parser');
require('dotenv').config();

const app = express();
const env = process.env;

const PORT_NUMBER = env.SERVER_PORT || 3002;

// MIDDLEWARE
// body parser
app.use(bodyParser.json());
app.use(
    bodyParser.urlencoded({
        extended: true,
    })
);


// Error handler middleware
app.use((err, req, res, next) => {
    const statusCode = err.statusCode || 500;
    console.error(err.message, err.stack);
    res.status(statusCode).json({ 'message': err.message });
    return;
});

// ROUTERS

// LISTEN PORT
app.listen(PORT_NUMBER, () => {
    console.log(`Started server at port: ${PORT_NUMBER}`)
});
