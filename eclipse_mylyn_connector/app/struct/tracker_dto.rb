class TrackerDto < ActionWebService::Struct
  member :id, :int
  member :name, :string

  def self.create tracker
    PriorityDto.new(
      :id => tracker.id,
      :name => tracker.name
    )
    rescue
      nil
  end
end