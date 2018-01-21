#!/bin/bash

DIRECTORY="$@"

for d in $DIRECTORY
do
	echo "processing files in directory $d"
	for f in $d/nfcapd*
	do
		echo "+- processing file $f"
		filename=${f##*/}
		echo $filename
		[[ $filename =~ ([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2}) ]]
		ft="${BASH_REMATCH[1]}-${BASH_REMATCH[2]}-${BASH_REMATCH[3]} ${BASH_REMATCH[4]}:${BASH_REMATCH[5]}"
		echo "   nfdump the data for $ft"
		../nfdump-1.6.15/bin/nfdump -o "fmt:,%ts,%te,$ft,%pr,%sa,%da,%sp,%dp,%pkt,%byt,%flg" -N -q  -r $f > ./extracted_data/flow_data_staging.${filename}.data
	done
done

for f in ./extracted_data/*
do
	echo "importing $f"
	mysqlimport --local -uroot -proot --fields-terminated-by=',' flow_score_source $f
	mysql -uroot -proot flow_score_source < sql/process_flow_data.sql 
	mv $f ./loaded_data/
done
