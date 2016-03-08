#
# Cookbook Name:: expo
# Recipe:: expo_install
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

directory '/vagrant/output' do
    owner 'spark'
    group 'spark'
    mode '0755'
    not_if { File.exist? '/vagrant/output' }
end

execute 'clear_output' do
  command 'rm -f *.json'
  cwd '/vagrant/output'
  only_if { File.exist? '/vagrant/output' }
end

local_data_file = '/tmp/text-analytics-expo-data-20160308.tar.gz'
local_data_location = '/vagrant/data/bbc'
remote_file local_data_file do
    source 'https://s3.amazonaws.com/3pillar-atg-backup/text-analytics-expo-data-20160308.tar.gz'
    not_if { File.exist? local_data_location }
end

execute 'untar' do
    command "tar -xzf #{local_data_file}"
    cwd '/vagrant'
    not_if { File.exist? local_data_location }
end

apt_package 'infop-expo' do
    action :remove
    ignore_failure true
end

dpkg_package 'infop-expo' do
    source '/vagrant/infop-expo/target/infop-expo_1.0-SNAPSHOT_all.deb'
end

group 'spark-users' do
    members ['spark', 'vagrant']
end

directory '/var/local/spark' do
  owner 'spark'
  group 'spark-users'
  mode '0775'
end
