#!/bin/bash

# Git workflow helper scripts

# Start new feature
start_feature() {
    git checkout develop
    git pull origin develop
    git checkout -b "feature/$1"
}

# Finish feature
finish_feature() {
    git checkout develop
    git pull origin develop
    git merge --no-ff "feature/$1"
    git push origin develop
    git branch -d "feature/$1"
}

# Start release
start_release() {
    git checkout develop
    git pull origin develop
    git checkout -b "release/$1"
}

# Finish release
finish_release() {
    git checkout main
    git pull origin main
    git merge --no-ff "release/$1"
    git tag -a "v$1" -m "Release version $1"
    git push origin main --tags
    
    git checkout develop
    git merge --no-ff "release/$1"
    git push origin develop
    git branch -d "release/$1"
}

# Hotfix
start_hotfix() {
    git checkout main
    git pull origin main
    git checkout -b "hotfix/$1"
}

finish_hotfix() {
    git checkout main
    git merge --no-ff "hotfix/$1"
    git tag -a "v$1" -m "Hotfix version $1"
    git push origin main --tags
    
    git checkout develop
    git merge --no-ff "hotfix/$1"
    git push origin develop
    git branch -d "hotfix/$1"
}

case "$1" in
    "feature-start") start_feature "$2" ;;
    "feature-finish") finish_feature "$2" ;;
    "release-start") start_release "$2" ;;
    "release-finish") finish_release "$2" ;;
    "hotfix-start") start_hotfix "$2" ;;
    "hotfix-finish") finish_hotfix "$2" ;;
    *) echo "Usage: $0 {feature-start|feature-finish|release-start|release-finish|hotfix-start|hotfix-finish} <name>" ;;
esac