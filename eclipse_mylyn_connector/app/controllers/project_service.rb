# 
# To change this template, choose Tools | Templates
# and open the template in the editor.

require File.dirname(__FILE__) + '/../api/project_api'
require File.dirname(__FILE__) + '/../struct/project_dto'

class ProjectService < ActionWebService::Base

  web_service_api ProjectApi
  def find_all 
    
    projects = Project.find  :all,
                  :conditions => Project.visible_by(User.current)
    projects.collect! {|x|ProjectDto.create(x)}

    return projects
  end
  
end
