# 
# To change this template, choose Tools | Templates
# and open the template in the editor.

require File.dirname(__FILE__) + '/../api/project_api'
require File.dirname(__FILE__) + '/../struct/project_dto'

class ProjectService < ActionWebService::Base

  web_service_api ProjectApi
  def find_all 
    
    projects = Project.find(:all, :joins => :enabled_modules,
                  :conditions => [ "enabled_modules.name = 'issue_tracking' AND #{Project.visible_by}"])
    projects.collect! {|x|ProjectDto.create(x)}

    return projects
  end
  
end
