# 
# To change this template, choose Tools | Templates
# and open the template in the editor.

require File.dirname(__FILE__) + '/../api/issue_api'
require File.dirname(__FILE__) + '/../struct/issue_dto'
require File.dirname(__FILE__) + '/../struct/issue_status_dto'
require File.dirname(__FILE__) + '/../struct/journal_dto'
require File.dirname(__FILE__) + '/../struct/attachment_dto'

class IssueService < BaseService
  web_service_api IssueApi

  def find_project rpcname, args
   if rpcname==:search_tickets
     @query = retrieve_query(args[0], args[1], args[2])
     @project = @query.project
   elsif rpcname==:find_tickets_by_last_update
     @project = Project.find(args[0])
   else 
     @issue = Issue.find(args[0])
     @project = @issue.project
   end
#rescue
#      false
  end

  def find_ticket_by_id(id)
    IssueDto.create(@issue)
  end
  
  def find_allowed_statuses_for_issue(id)
    statuses = @issue.new_statuses_allowed_to(User.current)
    
    if !statuses.include?(@issue.status) 
      statuses.unshift(@issue.status);
    end
    
    statuses.collect! {|x|IssueStatusDto.create(x)}
    return statuses.compact
  end
  
  def find_journals_for_issue(id)
    journals = @issue.journals.find(:all, :conditions => ["notes IS NOT NULL"])
    journals.collect! {|x|JournalDto.create(x)}
    return journals.compact
  end
  
  def find_attachments_for_issue(id)
    attachments = @issue.attachments
    attachments.collect! {|x|AttachmentDto.create(x)}
    return attachments
  end
  
  #project_id=2&set_filter=1&fields[]=start_date&operators[start_date]=t&values[start_date][]
  #project_id=2&set_filter=1&fields[]=status_id&operators[status_id]=o&values[status_id][]
  #project_id=2&set_filter=1&fields[]=updated_on&operators[updated_on]=>t-&values[updated_on][]=8
  def search_tickets(query_string, project_id, query_id)
    if @query.valid?
      issues = Issue.find :all,
                         :include => [ :assigned_to, :status, :tracker, :project, :priority, :category, :fixed_version ],
                         :conditions => @query.statement
      issues.collect! {|x|IssueDto.create(x)}
      return issues.compact
    else
      nil
    end
  end
  
  def find_tickets_by_last_update(projectid, timestamp)
    issues = Issue.find(:all, :conditions => ["project_id = ? AND updated_on >= ?", projectid, timestamp])
    issues.collect! {|x|x.id}
    return issues.compact
  end

  def find_relations_for_issue id
    relations = @issue.relations
    relations.collect! {|x|IssueRelationDto.create(x)}
    return relations.compact
  end
  
  private
  def retrieve_query query_string, project_id, query_id
    query = nil
    if project_id>0 && query_id>0 then
      project = Project.find(project_id)
      begin
        query = Query.find(query_id, :conditions => "project_id = #{project_id}")
      rescue
        query = Query.new
      end
      query.project = project
   else
      querydecoder = QueryStringDecoder.new(query_string)
      query = querydecoder.query
      query.project = querydecoder.project
    end
    return query
  end
  
end
