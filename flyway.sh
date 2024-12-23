#!/bin/bash

# ANSI color codes
RED="\033[31m"
GREEN="\033[32m"
BLUE="\033[34m"
YELLOW="\033[33m"
CYAN="\033[36m"
BOLD="\033[1m"
RESET="\033[0m"

# Flyway ASCII logo
display_logo() {
  echo -e "${RED}${BOLD}"
  echo "

    ███████╗██╗     ██╗   ██╗██╗    ██╗ █████╗ ██╗   ██╗     ██████╗ ███████╗███╗   ██╗███████╗██████╗  █████╗ ████████╗ ██████╗ ██████╗
    ██╔════╝██║     ╚██╗ ██╔╝██║    ██║██╔══██╗╚██╗ ██╔╝    ██╔════╝ ██╔════╝████╗  ██║██╔════╝██╔══██╗██╔══██╗╚══██╔══╝██╔═══██╗██╔══██╗
    █████╗  ██║      ╚████╔╝ ██║ █╗ ██║███████║ ╚████╔╝     ██║  ███╗█████╗  ██╔██╗ ██║█████╗  ██████╔╝███████║   ██║   ██║   ██║██████╔╝
    ██╔══╝  ██║       ╚██╔╝  ██║███╗██║██╔══██║  ╚██╔╝      ██║   ██║██╔══╝  ██║╚██╗██║██╔══╝  ██╔══██╗██╔══██║   ██║   ██║   ██║██╔══██╗
    ██║     ███████╗   ██║   ╚███╔███╔╝██║  ██║   ██║       ╚██████╔╝███████╗██║ ╚████║███████╗██║  ██║██║  ██║   ██║   ╚██████╔╝██║  ██║
    ╚═╝     ╚══════╝   ╚═╝    ╚══╝╚══╝ ╚═╝  ╚═╝   ╚═╝        ╚═════╝ ╚══════╝╚═╝  ╚═══╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝

  "
  echo -e "${RESET}"
}

# Function to generate the timestamp prefix
generate_timestamp_prefix() {
  date +"%Y%m%d%H%M%S"
}

# Function to create the migration script
create_migration_script() {
  local migration_name=$1
  local timestamp_prefix=$(generate_timestamp_prefix)

  if [ -z "$migration_name" ]; then
    echo -e "${RED}${BOLD}Error:${RESET} Migration name cannot be empty."
    exit 1
  fi

  # Retrieve the author's name from git config
  local author_name=$(git config user.name)
  if [ -z "$author_name" ]; then
    author_name="Unknown Author"
  fi

  # Get the current date
  local creation_date=$(date +"%Y-%m-%d %H:%M:%S")

  # Set the folder path
  local migration_folder="src/main/resources/db/migration"
  local migration_file="V${timestamp_prefix}__${migration_name}.sql"

  # Create the folder if it doesn't exist
  mkdir -p "$migration_folder"

  # Full path to the migration file
  local migration_file_path="${migration_folder}/${migration_file}"

  # Add metadata and SQL migration template to the file
  echo "-- Flyway migration script: $migration_file" > "$migration_file_path"
  echo "-- Author: $author_name" >> "$migration_file_path"
  echo "-- Created on: $creation_date" >> "$migration_file_path"
  echo "-- Add your SQL migration statements here." >> "$migration_file_path"

  echo -e "${GREEN}${BOLD}Migration script created successfully:${RESET} ${migration_file_path}"
}

# Main CLI function
main() {
  display_logo

  # Prompt for migration name
  echo -e "${YELLOW}${BOLD}Enter the migration name:${RESET}"
  read -p "> " migration_name

  # Create the migration script
  create_migration_script "$migration_name"

  echo -e "${BLUE}${BOLD}Done! Happy migrating with Flyway!${RESET}"
}

# Run the script
main
