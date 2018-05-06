# Clipboard Synchronization

This application share data between multiple connected machines using Socket connection. The data is pushed to other connected machines in real time, when clipboard content of anyone of the machine changes.

- [Downloads](https://github.com/vishrantgupta/sync-clipboard/blob/master/ClipboardSync.jar)
- [Web Site](http://vishrantgupta.info/)

![main window](https://github.com/vishrantgupta/sync-clipboard/blob/master/screenshots/clipboard_sync.png)

## Overview

Clipboard Synchronization manager monitor system clipboard content, if there is any new content in clipboard it is broadcasted to all the connected systems

## Features

* Support for Linux, Windows and OS X 10.9+
* Paste copied content in other connected system with ctrl + v

## Requirement

* JRE 1.8 or higher

## Install

In order to run the clipboard synchronization manager it is required to install JRE version 1.8 or higher.
JRE can be downloaded from [Oracle Java Website] (http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

### Linux

Install JRE in 

```bash
sudo apt update; sudo apt install oracle-java8-installer
```

## Using the App

To start the application double-click the application file. Request secret code after entering email id. Using that secret code start synchronization. Use the same email id and secret code in all the systems where you want to sync your clipboard


