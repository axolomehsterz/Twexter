const HtmlWebpackPlugin = require("html-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CopyPlugin = require("copy-webpack-plugin");
const path = require("path");
const devMode = process.env.NODE_ENV === "production";
const styleLoader = devMode ? MiniCssExtractPlugin.loader : "style-loader";

const BACKEND = "spring-boot" || "express";

const config = {
  // JS that gets run in index.html
  entry: "./index.js",
  // Either 'development' or 'production'
  mode: "development",
  // When npm run build is executed...
  output: {
    // Output goes here
    path: path.resolve(__dirname, "./dist"),
    // Uglified / minified JS file
    filename: "index_bundle.js",
  },
  // Development server settings
  devServer: {
    // Serves front end on port 3000
    port: "8080",
    // Serves everything in the client folder
    host: "localhost",
    static: {
      directory: path.join(__dirname, "./dist"),
      publicPath: "/",
    },
    // Automatically opens the browser after starting the server
    open: true,
    // Fetch requests in the front end are directed to this URL
    proxy: {
      "/": "http://localhost:3000",
    },
    historyApiFallback: true,
  },
  // Everything seems to work when this is commented out. Do I really need this?
  resolve: {
    extensions: [".js", ".jsx", ".json", ".css", "..."],
  },
  module: {
    rules: [
      // Parse JS and JSX files
      {
        // File names
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: [
              ["@babel/preset-env", { targets: "defaults" }],
              ["@babel/preset-react", { targets: "defaults" }],
            ],
          },
        },
      },
      {
        test: /\.css$/i,
        use: [styleLoader, "css-loader", "postcss-loader"],
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: "asset/resource",
      },
    ],
  },
  // Plugins serve the purpose of doing anything that a loader cannot do
  plugins: [
    // Automatically generate an HTML5 file that includes all webpack bundles
    new HtmlWebpackPlugin({
      // Takes ./client/index.html, and injects:
      // <script defer src="index_bundle.js"></script>
      // into <head></head>
      // I think this index_bundle.js file is not created, but read from RAM during development
      template: path.join(__dirname, "client", "./index.html"),
      filename: "./index.html",
    }),

    new MiniCssExtractPlugin({
      filename: "main.css",
    }),
    new CopyPlugin({
      patterns: [{ from: path.join(__dirname, "./client/images") }],
    }),

    // new CopyPlugin({
    //   patterns: [{ from: path.join(__dirname, './client/images'), to: './' }],
    // }),
  ],
};

if (BACKEND === 'spring-boot') {
  const filePath = "./spring-server/src/main/resources/static"
  config.output.path = path.resolve(__dirname, filePath);
  config.devServer.static.directory = path.join(__dirname, filePath);
}
module.exports = config;