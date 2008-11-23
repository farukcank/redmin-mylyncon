require 'redmine'

RAILS_DEFAULT_LOGGER.info 'Starting mylyn-connector plugin for RedMine'


Redmine::Plugin.register :mylyn_connector do
  name 'Mylyn connector plugin'
  author 'Sven Krzyzak'
  description 'This plugin provides a webservice API for Eclipse Mylyn'
  version '3.0.0v2'
end
