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

user 'vagrant' do
  home '/home/vagrant'
  shell '/bin/bash'
  password '$1$m3XIKd6L$zNI77cFExa1xJ798mSbvq.'
  manage_home true
  not_if { File.exist? '/home/vagrant' }
end

file '/etc/profile.d/jdk_path.sh' do
  content "export PATH=/usr/lib/jvm/java-8-oracle-amd64/bin:$PATH"
  mode 00755
end
