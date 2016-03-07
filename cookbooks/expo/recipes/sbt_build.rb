#
# Cookbook Name:: expo
# Recipe:: sbt_build
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
execute 'expo_debian_package' do
  command "sbt debian:packageBin"
  cwd "/vagrant/infop-expo"
  user "vagrant"
end
