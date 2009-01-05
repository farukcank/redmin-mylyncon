
require File.dirname(__FILE__) + '/../api/project_based_api'
require File.dirname(__FILE__) + '/../struct/tracker_dto'
require File.dirname(__FILE__) + '/../struct/issue_category_dto'
require File.dirname(__FILE__) + '/../struct/member_dto'
require File.dirname(__FILE__) + '/../struct/version_dto'
require File.dirname(__FILE__) + '/../struct/issue_status_dto'
require File.dirname(__FILE__) + '/../struct/issue_custom_field_dto'

class ProjectBasedService < BaseService
  web_service_api ProjectBasedApi

  def find_project rpcname, args
    @project = Project.find(args[0])
    rescue
      false
  end

  def get_trackers_for_project id
    tmp = @project.trackers.find(:all);
    trackers = Array.new(tmp.size)
    tmp.each { |element| 
      trackers.push(TrackerDto.create(element))
    }
  end
  
  def get_issue_custom_fields_for_project id
    custom_fields = @project.methods.include?('all_issue_custom_fields') ? @project.all_issue_custom_fields : @project.all_custom_fields;
    custom_fields.collect! { |x| IssueCustomFieldDto.create(x) }
    return custom_fields.compact
  end
  
  def get_issue_categorys_for_project id
    tmp = @project.issue_categories
    categorys = Array.new(tmp.size)
    tmp.each { |element| 
      categorys.push(IssueCategoryDto.create(element))
    }
  end

  def get_members_for_project id
    members = @project.members
    members.collect!{|x|MemberDto.create(x)}
    return members
  end

  def get_versions_for_project id
    versions = @project.versions
    versions.collect!{|x|VersionDto.create(x)}
    return versions
  end

  def get_queries_for_project id
    # Code form Issue_helper
    visible = ARCondition.new(["is_public = ? OR user_id = ?", true, User.current.id])
    visible << (@project.nil? ? ["project_id IS NULL"] : ["project_id IS NULL OR project_id = ?", @project.id])
    queries = Query.find(:all,
                         :order => "name ASC",
                         :conditions => visible.conditions)
    queries.collect!{|x|QueryDto.create(x)}
    return queries.compact
  end


end