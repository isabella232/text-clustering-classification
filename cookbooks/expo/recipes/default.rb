#
# Cookbook Name:: expo
# Recipe:: default
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

include_recipe "expo::deb_deps"
include_recipe "expo::sbt_build"
include_recipe "expo::expo_install"
