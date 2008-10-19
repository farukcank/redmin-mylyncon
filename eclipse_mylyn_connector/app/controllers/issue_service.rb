# 
# To change this template, choose Tools | Templates
# and open the template in the editor.

require File.dirname(__FILE__) + '/../api/issue_api'
require File.dirname(__FILE__) + '/../struct/issue_dto'
require File.dirname(__FILE__) + '/../struct/issue_status_dto'
require File.dirname(__FILE__) + '/../struct/journal_dto'

class IssueService < BaseService
  web_service_api IssueApi

  def find_project rpcname, args
    if rpcname==:search_tickets
      @querydecoder = QueryStringDecoder.new(args[0])
      @project = @querydecoder.project
    elsif rpcname==:find_tickets_by_last_update
      @project = Project.find(args[0])
    elsif rpcname==:update_ticket
      @issue = Issue.find(args[0]['id'])
      @project = @issue.project
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
    journals = @issue.journals
    journals.collect! {|x|JournalDto.create(x)}
    return journals.compact
  end
  
  #project_id=2&set_filter=1&fields[]=start_date&operators[start_date]=t&values[start_date][]
  #project_id=2&set_filter=1&fields[]=status_id&operators[status_id]=o&values[status_id][]
  #project_id=2&set_filter=1&fields[]=updated_on&operators[updated_on]=>t-&values[updated_on][]=8
  def search_tickets(query_string)
    query = @querydecoder.query
    if query.valid?
      issues = Issue.find :all,
                          :include => [ :assigned_to, :status, :tracker, :project, :priority, :category, :fixed_version ],
                          :conditions => query.statement

      issues.collect! {|x|IssueDto.create(x)}
      return issues.compact
    else
      nil
    end
  end
  
  def find_tickets_by_last_update(projectid, timestamp)
    issues = Issue.find :all,
                        :conditions => ["project_id = ? AND updated_on >= ?", projectid, timestamp]
    issues.collect! {|x|x.id}
    return issues.compact
 end
  
  def update_ticket(issueDto, comment)
    allowed_statuses = @issue.new_statuses_allowed_to(User.current)
    edit_allowed = User.current.allowed_to?(:edit_issues, @project)
    
    # User can change issue attributes only if he has :edit permission or if a workflow transition is allowed
    if (edit_allowed || !allowed_statuses.empty?)
      attrs = IssueDto.to_hash(issueDto);
    
      attrs.delete_if {|k,v| !IssuesController::UPDATABLE_ATTRS_ON_TRANSITION.include?(k) } unless edit_allowed
      attrs.delete('status_id') unless allowed_statuses.detect {|s| s.id.to_s == attrs['status_id'].to_s}
    
      journal = @issue.init_journal(User.current, comment)
      @issue.attributes=attrs
      res=@issue.save

      if !journal.new_record?
        # Only send notification if something was actually changed
        Mailer.deliver_issue_edit(journal) if Setting.notified_events.include?('issue_updated')
      end
      
      return res
    end
    return false
  end
end
