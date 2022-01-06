require('dotenv').config();

const env = process.env;

const config = {
    db: {
        host: env.DB_HOST_LC,
        port: env.DB_PORT_LC,
        user: env.DB_USER_LC,
        password: env.DB_PASSWD_LC,
        database: env.DB_NAME_LC
    },
};


module.exports = config;
