#!/bin/bash

# Set up variables for backup paths
BACKUP_DIR="/mnt/backup"
TIMESTAMP=$(date +"%F-%H-%M-%S")
BACKUP_PATH="$BACKUP_DIR/backup-$TIMESTAMP.tar.gz"

# Define directories to back up
DIRECTORIES_TO_BACKUP=(
  "InventoryService"
  "OrderService"
  "PaymentService"
  "NotificationService"
  "RecommendationService"
  "serverless"
  "frontend"
  "mobile/android"
  "mobile/ios"
  "config"
  "docker"
  "k8s"
)

# Create a backup directory if it doesn't exist
mkdir -p $BACKUP_DIR

# Compress and create the backup
tar -czvf $BACKUP_PATH "${DIRECTORIES_TO_BACKUP[@]}"

# Print success message
echo "Backup completed successfully at $BACKUP_PATH"

# Upload the backup to a remote server
REMOTE_SERVER="user@backup-server:/remote/backup/location"
scp $BACKUP_PATH $REMOTE_SERVER

# Clean up old backups (older than 7 days)
find $BACKUP_DIR -type f -mtime +7 -name '*.tar.gz' -exec rm -f {} \;

echo "Old backups cleaned up successfully."