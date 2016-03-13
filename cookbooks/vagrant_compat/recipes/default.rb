#
# Cookbook Name:: vagrant_compat
# Recipe:: default
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

apt_update 'apt_update' do
  action :update
end

user 'vagrant' do
  home '/home/vagrant'
  shell '/bin/bash'
  password '$1$m3XIKd6L$zNI77cFExa1xJ798mSbvq.'
  manage_home true
  not_if { File.exist? '/home/vagrant' }
end
