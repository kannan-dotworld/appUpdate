const express = require('express')
const app = express();
const downloadRouter = require("./route/download");
const apklink = require("./route/apklink");

 app.listen(3001, () => {
    console.log(`Example app listening at http://localhost:${3001}`)
  })

app.use("/download", downloadRouter);
app.use("/apklink", apklink);