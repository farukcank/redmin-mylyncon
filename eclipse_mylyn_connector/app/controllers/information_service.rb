
require File.dirname(__FILE__) + '/../api/information_api'

class InformationService < ActionWebService::Base
  web_service_api InformationApi
  
  def get_redmine_version
    Redmine::VERSION
  end
  
  def get_rails_version
    RAILS_GEM_VERSION
  end
  
  def check_credentials username, password
    if User.try_to_login(username, password);
      true
    else
      false
    end
  end
  
end