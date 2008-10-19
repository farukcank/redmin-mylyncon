class InformationApi < ActionWebService::API::Base
  api_method :get_redmine_version,
    :returns => [:string]

  api_method :get_rails_version,
    :returns => [:string]

  api_method :check_credentials,
    :expects => [:string, :string],
    :returns => [:bool]
end