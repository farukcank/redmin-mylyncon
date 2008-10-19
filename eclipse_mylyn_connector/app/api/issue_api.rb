# 
# To change this template, choose Tools | Templates
# and open the template in the editor.
 
require File.dirname(__FILE__) + '/../struct/issue_dto'
require File.dirname(__FILE__) + '/../struct/issue_status_dto'
require File.dirname(__FILE__) + '/../struct/journal_dto'

class IssueApi < ActionWebService::API::Base
  api_method :find_ticket_by_id,
    :expects => [:int],
    :returns => [IssueDto]
  
  api_method :find_allowed_statuses_for_issue,
    :expects => [:int],
    :returns => [[IssueStatusDto]]

  api_method :find_journals_for_issue,
    :expects => [:int],
    :returns => [[JournalDto]]

  api_method :search_tickets,
    :expects => [:string],
    :returns => [[IssueDto]]
  
  api_method :find_tickets_by_last_update,
    :expects => [:int, :datetime],
    :returns => [[:int]]
  
  api_method :update_ticket,
    :expects => [IssueDto, :string],
    :returns => [:boolean]
end
