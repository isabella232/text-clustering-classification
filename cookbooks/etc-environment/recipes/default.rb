#
# Cookbook Name:: etc-environment
# Recipe:: default
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

apt_update 'apt_update' do
  action :update
end

env_key = 'JAVA_OPTS'
env_file = '/etc/environment'
bash "java_options" do
    code <<-EOH
      echo >> #{env_file}
      echo '#{env_key}="-Xms1024M -Xmx2048M -Xss512M"' >> #{env_file}
      EOH
    not_if { ::ENV[env_key] }
end
