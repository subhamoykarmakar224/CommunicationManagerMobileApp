const db = require('./db');
const readline = require('readline');
const fs = require('fs');
const helper = require('../utils/helper');

async function insertSensorData() {

    const rows = await db.query(
        `select name, phone from occupancy`,
        []
    );
    const data = helper.emptyOrRows(rows);

    return {
        data
    }
}

module.exports = {
    getOccupancy
}