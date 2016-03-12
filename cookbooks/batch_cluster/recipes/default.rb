#
# Cookbook Name:: batch_cluster
# Recipe:: default
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

execute "chown-vagrant-everything" do
  command "chown -R vagrant: *"
  cwd "/vagrant"
end

execute 'batch_cluster_assembly' do
  command "sbt assembly"
  cwd "/vagrant/batch-cluster"
  user "vagrant"
end
