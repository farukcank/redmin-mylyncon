
require File.dirname(__FILE__) + '/../struct/tracker_dto'
require File.dirname(__FILE__) + '/../struct/issue_category_dto'
require File.dirname(__FILE__) + '/../struct/member_dto'
require File.dirname(__FILE__) + '/../struct/version_dto'
require File.dirname(__FILE__) + '/../struct/issue_status_dto'

class ProjectBasedApi < ActionWebService::API::Base
  api_method :get_trackers_for_project,
    :expects => [:int],
    :returns => [[TrackerDto]]
  
  api_method :get_issue_categorys_for_project,
    :expects => [:int],
    :returns => [[IssueCategoryDto]]

  api_method :get_members_for_project,
    :expects => [:int],
    :returns => [[MemberDto]]

  api_method :get_versions_for_project,
    :expects => [:int],
    :returns => [[VersionDto]]

end