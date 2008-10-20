class IssueDto < ActionWebService::Struct
#:category, :status, :priority, :fixed_version, :start_date, :done_ration, :estimated_hours
  member :id, :int
  member :project_id, :int
  member :subject, :string
  member :description, :string
  member :author, :string
  member :assigned_to_id, :int
  member :created_on, :datetime
  member :updated_on, :datetime
  member :priority_id, :int
  member :category_id, :int
  member :version_id, :int
  member :tracker_id, :int
  member :status_id, :int
  
  def self.create issue
    return IssueDto.new(
      :id => issue.id,
      :project_id => issue.project_id,
      :subject => issue.subject,
      :description => issue.description,
      :author => issue.author.name,
      :assigned_to_id => issue.assigned_to_id,
      :created_on => issue.created_on,
      :updated_on => issue.updated_on,
      :priority_id => issue.priority_id,
      :category_id => issue.category_id,
      :version_id => issue.fixed_version_id,
      :tracker_id => issue.tracker_id,
      :status_id => issue.status_id
    )
#    rescue
#      nil
  end
  
  def self.to_hash dto
    attrs = Hash.new
#    self.instance_variables.each {|k| attrs[k.sub(/^@/, '')]=self.instance_variable_get(k)}
    dto.each {|k,v| attrs[k]=v}
    
    attrs['fixed_version_id'] = attrs['version_id']
    attrs.delete('version_id')
    attrs.delete('project_id')
    attrs.delete('tracker_id')
    attrs.delete('author')
    
    return attrs
  end
end