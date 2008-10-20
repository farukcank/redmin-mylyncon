
require File.dirname(__FILE__) + '/../api/information_api'

class InformationService < ActionWebService::Base
  
  web_service_api InformationApi
  
  def get_version
    return Array.[](Redmine::VERSION, RAILS_GEM_VERSION,  Redmine::Plugin.registered_plugins[:mylyn_connector].version)
  end
  
  def check_credentials username, password
    if User.try_to_login(username, password);
      true
    else
      false
    end
  end
  
end