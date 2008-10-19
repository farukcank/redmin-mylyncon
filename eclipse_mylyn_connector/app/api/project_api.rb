# 
# To change this template, choose Tools | Templates
# and open the template in the editor.
 
require File.dirname(__FILE__) + '/../struct/project_dto'

class ProjectApi < ActionWebService::API::Base
  api_method :find_all,
    :returns => [[ProjectDto]]
end
