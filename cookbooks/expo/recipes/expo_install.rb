#
# Cookbook Name:: expo
# Recipe:: expo_install
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

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
