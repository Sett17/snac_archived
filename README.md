## Project on hold, latest commit is not in working state
commits 3439da0d6eeb6664220c2a56c6ff7cee20a0b5f4 or 6d795972529fc3f7db596ce29ca2cf91ad32ec3b may work

<div align="center">

  <h1>Snac</h1>

  <p>
    snac is <i>the</i> snippet manager you, or your small Team, needs. Ever google the same thing multiple times a week, and then see your colleague also searching for the same piece of code? <b>No more!</b>

Now you can easily use snippets in any language (or in no language?!), with a simple table making use of Postgresql features, and a clean looking web frontend inspired by [Colors of GitHub Copilot](https://github.com/features/copilot).

<i>This is a rewrite in Go and VanillaJS from the Fullstack Kotlin version before. The Kotlin version is no longer being maintained but is accessible on the [kotlin-version branch](http://github.com/Sett17/snac/tree/kotlin-version)</i>
  </p>


<!-- Badges -->
<p>
  <a href="https://github.com/Sett17/Devfile/graphs/contributors">
    <img src="https://img.shields.io/github/contributors/Sett17/snac" alt="contributors" />
  </a>
  <a href="">
    <img src="https://img.shields.io/github/last-commit/Sett17/snac" alt="last update" />
  </a>
  <a href="https://github.com/Sett17/snac/network/members">
    <img src="https://img.shields.io/github/forks/Sett17/snac" alt="forks" />
  </a>
  <a href="https://github.com/Sett17/snac/stargazers">
    <img src="https://img.shields.io/github/stars/Sett17/snac" alt="stars" />
  </a>
  <a href="https://github.com/Sett17/snac/issues/">
    <img src="https://img.shields.io/github/issues/Sett17/snac" alt="open issues" />
  </a>
</p>
</div>

<br />


## Screenshot
![](img.png)


## Installation

Download the distribution zip and create a `config.yaml` from the the `config-template.yaml` to your liking and start the server.

```bash
./snac
```

The sql code to create the table and the needed function dn trigger in the [sql/](sql/).
The `table.sql` file should be executed last.

*NOTE: The sql files assume a user called `snac`.*

## Building

Clone this repo
```bash
git clone https://github.com/Sett17/snac.git
```
and run the go build command to build the binary.
```bash
go build .
```

## Config

Currently, all items in the `config-template.yaml` are needed.

Explanations for each configuration key are given in the `config-template.yaml` file.

## Frontend Usage

### Shortcuts
| Key          | Description                                                          | Context    |
|--------------|----------------------------------------------------------------------|------------|
| `ctrl+s`     | Saves the current snippet                                            | Everywhere |
| `ctrl+enter` | Re-Highlights the Snippet (is also done automatically after editing) | in Editor  |
| `tab`        | Inserts 2 spaces in current cursor position                          | in Editor  |

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)
