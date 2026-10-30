#Environment variables


variable "nuska_service_account" {
  description = "application service account"
}

variable "labels" {
  description = "Labels used in all resources"
  type = map(string)
  default = {
    manager = "terraform"
    team = "ror"
    slack = "talk-ror"
    app = "nuska"
  }
}

variable "bucket_instance_name" {
  description = "Storage Bucket name"
}

variable "service_account_bucket_role" {
  description = "Role of the Service Account - more about roles https://cloud.google.com/storage/docs/access-control/iam-roles"
  default = "roles/storage.objectViewer"
}
