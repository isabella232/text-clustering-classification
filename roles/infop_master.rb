name "infop_master"
description "Role for master node of an information palace (cluster)"

run_list(
  "recipe[vagrant_compat]",
  "recipe[oracle_java_8]",
  "recipe[scala]",
  "recipe[simple-scala-sbt]",
  "recipe[apache_spark::spark-standalone-master]",
  "recipe[apache_spark::spark-standalone-worker]",
  "recipe[batch_cluster]",
  "recipe[expo]"
)

default_attributes(
  :java => {
    :oracle => {
      :accept_oracle_download_terms => true
    },
    :jdk_version => 8,
    :install_flavor => 'oracle',
    :jdk => {
      '8' => {
        'x86_64' => {
          :url => 'https://s3.amazonaws.com/3pillar-atg-backup/jdk-8u65-linux-x64.tar.gz',
          :checksum => '88db2aacdc222c2add4d92822f528b7a2101552272db4487f33b38b0b47826e7'
        }
      }
    }
  },

  :scala => {
    :version => "2.11.6",
    :url => "http://downloads.lightbend.com/scala/2.11.6/scala-2.11.6.tgz",
    :checksum => "41ba45e4600404634217a66d6b2c960459d3a67e0344a7c3d9642d0eaa446583"
  },

  'simple-scala-sbt' => {
    :version => "0.13.9"
  }
)
