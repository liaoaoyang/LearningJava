#!/bin/sh

check_jar()
{
    base_dir=$(cd `dirname $1`; pwd)
    target_dir=$base_dir/../target

    if [ ! -d $target_dir ];then
        echo "No target directory, please run mvn package first"
        exit
    fi

    if [ ! -z $2 ];then
        if [ ! -f $target_dir/$2".jar" ];then
            echo "No "$target_dir/$2".jar, please run mvn package first"
            exit
        fi
    fi
}

get_jar_filename()
{
    base_dir=$(cd `dirname $1`; pwd)
    echo $base_dir"/../target/"$2".jar"
}