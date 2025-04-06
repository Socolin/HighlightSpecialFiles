# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

### 2025.1.1
- Fix "Always select open file" feature for nested files

### 2025.1.0
- Update to rider 2025.1

### 2024.3.0
- Update to rider 2024.3

### 2024.1.3
- Fix virtual folders content getting duplicated cause the id was not globally unique

### 2024.1.2
- Fix crash

### 2024.1.1
- Fix virtual folder changing expanded state when adding/removing files in it
- Add option to create virtual folder only when there is a given ammount of files to group
- Polish UI

### 2024.1.0
- Update to rider 2024.1

### 1.7.0
- Update to rider 2023.3-EAP8

### 1.6.0
- Add nesting rules

### 1.5.0

- Apply rule to change icon in Tabs and in the Switcher([Ctrl]+[Tab])
- Add option to disable a rule from the configuration
- Fix: Virtual folder no longer getting marked as error when a neighbor file is in error
- Fix: Navigate To Explorer / Always Select Opened File now works with virtual folders

## 1.4.3

- Fix applying rules on files in attached folder

## 1.4.2

- Add button to access the rules configuration from the solution explorer view

## 1.4.1

- Update to rider 2023.2-EAP6

## 1.4.0

- Update to rider 2023.1
- Add new options on rules to regroup multiple files in a virtual folder
- Add some built-in icons
- Add a button to duplicate a rule
- Fix rule ordering not always honoured

## 1.3.1


- Support Rider 2023.1-EAP8

## 1.3.0

- Support Rider 2023.1-EAP4

## 1.2.2

- Fix loading icons on windows

## 1.2.1

- Fix adding rules

## 1.2.0

- Support loading icons from `.idea` folder so you can now use any icons you want. It supports svg and png.
- Polish UI and add UI for managing icons

## 1.1.0

- Allow to store rules into 2 configurations files: `.user.xml` that should not be committed and `.project.xml` that should be committed to share rule in a project
- Rework UI

## 1.0.0

- Initial version
