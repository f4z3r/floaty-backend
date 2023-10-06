terraform {
  required_version = "=1.3.7"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=3.27.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "~> 2.30.0"
    }
  }

  # NOTE: Local state files are for some basic testing only - not recommended in dev or production.
#  backend "local" {
#    path = "terraform.tfstate"
#  }

  #  NOTE: This would be the proper way to handle a state file backend - hosted on remote storage with locking.
  #        This might be used when IaC will be used within a CI/CD pipeline.
  #
    backend "azurerm" {
      resource_group_name  = "rg-floaty-terraform-backend"
      storage_account_name = "floatyterraformbackend"
      container_name       = "terraformstatefiles"
      key                  = "infrastructure.floaty.tfstate"
    }
}

provider "azurerm" {
  features {}
}

locals {
  app_context     = "floaty-chn" # used for multiple services which cannot have uppercase letters, chn
  app_context_weu = "floaty-weu" # used for multiple services which cannot have uppercase letters, weu
  tags = {
    "expireOn" = "2023-12-31"
    "owner"    = "matthaeus.heer@ipt.ch"
  }
}

# ----- SECTION: Resource group ------
resource "azurerm_resource_group" "floaty" {
  name     = format("rgr-%s", local.app_context)
  location = "Switzerland North"
  tags     = local.tags
  lifecycle {
    ignore_changes = [
      tags
    ]
  }
}

# ----- SECTION: Front-end hosting (Azure Static App) -----
resource "azurerm_static_site" "floaty" {
  name                = format("ss-%s", local.app_context_weu)
  resource_group_name = azurerm_resource_group.floaty.name
  location            = "West Europe" # Switzerland North not available 
  sku_tier            = "Standard"
  sku_size            = "Standard"
  lifecycle {
    ignore_changes = [
      tags
    ]
  }
}

# ----- SECTION: API Management with API and Security policies ----- #
resource "azurerm_api_management" "floaty" {
  name                = format("apim-%s", local.app_context)
  resource_group_name = azurerm_resource_group.floaty.name
  location            = azurerm_resource_group.floaty.location
  publisher_name      = "floaty-corporation"
  publisher_email     = "matthaeus.heer@ipt.ch"
  sku_name            = "Developer_1"
  identity {
    type = "SystemAssigned"
  }
  lifecycle {
    ignore_changes = [
      tags
    ]
  }
}

resource "azurerm_api_management_api" "floaty" {
  name                = format("api-%s", local.app_context)
  resource_group_name = azurerm_resource_group.floaty.name
  api_management_name = azurerm_api_management.floaty.name
  revision            = "1"
  display_name        = "floaty-api"
  path                = "api"
  protocols           = ["https"]

  import {
    content_format = "openapi"
    content_value  = file("../api/floaty-api.yml")
  }
}

resource "azurerm_api_management_api_policy" "floaty" {
  api_name            = azurerm_api_management_api.floaty.name
  api_management_name = azurerm_api_management_api.floaty.api_management_name
  resource_group_name = azurerm_api_management_api.floaty.resource_group_name
  xml_content         = <<XML
<policies>
    <inbound>
        <base />
        <set-backend-service id="apim-generated-policy" backend-id="${azurerm_api_management_backend.floaty.name}"/>
        <authentication-managed-identity resource="${data.azuread_service_principal.floaty.application_id}" output-token-variable-name="msi-access-token" ignore-error="false" />
        <set-header name="Authorization" exists-action="override">
            <value>@("Bearer " + (string)context.Variables["msi-access-token"])</value>
        </set-header>
    </inbound>
    <backend>
        <base />
    </backend>
    <outbound>
        <base />
    </outbound>
    <on-error>
        <base />
    </on-error>
</policies>
XML
}

resource "azurerm_api_management_backend" "floaty" {
  name                = format("apim-%s-backend", local.app_context)
  resource_group_name = azurerm_resource_group.floaty.name
  api_management_name = azurerm_api_management.floaty.name
  protocol            = "http"
  url                 = "https://${azurerm_linux_web_app.app_service_floaty_backend.name}.azurewebsites.net"
}

data "azuread_service_principal" "floaty" {
  display_name = "sp-floaty"
}

# ----- SECTION: MS SQL Database ----- #
resource "azurerm_mssql_server" "floaty" {
  name                         = format("sqlserver-%s", local.app_context)
  resource_group_name          = azurerm_resource_group.floaty.name
  location                     = azurerm_resource_group.floaty.location
  version                      = "12.0"
  administrator_login          = "floaty-user"
  administrator_login_password = "floaty-password"
  lifecycle {
    ignore_changes = [
      tags
    ]
  }
}

resource "azurerm_mssql_database" "floaty" {
  name           = format("db-%s", local.app_context)
  server_id      = azurerm_mssql_server.floaty.id
  license_type   = "LicenseIncluded"
  max_size_gb    = 2
  read_scale     = false
  sku_name       = "S0"
  zone_redundant = false
  lifecycle {
    ignore_changes = [
      license_type, # seems to be a bug, tf always wants to add this although already configured
      tags
    ]
  }
}

#     CAUTION: This resource needs to be commented out when importing e.g., the resource group at the beginning
#
#     NOTE: For this to work, the azurerm_linux_web_app has to be in place ALREADY
#     Therefore perform the provisioning in two steps one after the other
#           1) terraform apply -target="azurerm_linux_web_app.app_service_floaty_backend"
#           2) terraform apply
#       Step 1) makes sure the app service is in place and outbound ip addresses can be used within the set
#       Step 2) provisions all the rest

# NOTE: Resource needs to be COMMENTED OUT when importing the resource group, then follow step 1) and 2)
resource "azurerm_mssql_firewall_rule" "floaty" {
  name             = "web_app_ip_${replace(each.value, ".", "_")}"
  server_id        = azurerm_mssql_server.floaty.id
  for_each         = toset(azurerm_linux_web_app.app_service_floaty_backend.outbound_ip_address_list)
  start_ip_address = each.value
  end_ip_address   = each.value
}

# ----- SECTION: App Service Back-end Service ----- #
resource "azurerm_service_plan" "floaty" {
  name                = format("asp-%s", local.app_context)
  resource_group_name = azurerm_resource_group.floaty.name
  location            = azurerm_resource_group.floaty.location
  os_type             = "Linux"
  sku_name            = "B1"
  lifecycle {
    ignore_changes = [
      tags
    ]
  }
}

resource "azurerm_linux_web_app" "app_service_floaty_backend" {
  name                    = format("wa-%s", local.app_context)
  resource_group_name     = azurerm_resource_group.floaty.name
  location                = azurerm_service_plan.floaty.location
  service_plan_id         = azurerm_service_plan.floaty.id
  https_only              = true
  client_affinity_enabled = true

  app_settings = {
    WEBSITES_PORT                       = "8080"
    DOCKER_REGISTRY_SERVER_URL          = "https://docker.io"
    WEBSITES_ENABLE_APP_SERVICE_STORAGE = "false"
  }

  # There is a bug (https://github.com/hashicorp/terraform-provider-azurerm/issues/12928#issuecomment-1092659395)
  # regarding OAuth v2 support.
  # Hence, instead of fixing this nicely, one has to configure Authentication manually in the portal.
  # This can be done by going to...
  # -> App Service -> Authentication -> Upgrade (once) -> Add Provider -> Microsoft
  # -> Pick existing App Registration in this directory -> Pick App registration for App Service from list
  # -> Set HTTP401 for unauthorized access
  #
  #  auth_settings {
  #    enabled         = true
  #    runtime_version = "~2"
  #    active_directory {
  #      client_id                  = data.azuread_service_principal.floaty.application_id
  #    }
  #  }

  # Enables application logs in log stream
  logs {
    http_logs {
      file_system {
        retention_in_days = 5
        retention_in_mb   = 35
      }
    }
  }

  site_config {}

  lifecycle {
    ignore_changes = [
      tags
    ]
  }

}

