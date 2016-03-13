#
# Cookbook Name:: oracle_java_8
# Recipe:: default
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

include_recipe "java"

env_key = 'JAVA_OPTS'
env_file = '/etc/environment'
bash "java_options" do
    code <<-EOH
      echo >> #{env_file}
      echo '#{env_key}="-Xms1024M -Xmx2048M -Xss512M"' >> #{env_file}
      EOH
    not_if { ::ENV[env_key] }
end

execute "set oracle java8 as default" do
  command "update-alternatives --set java /usr/lib/jvm/java-8-oracle-amd64/bin/java"
end
