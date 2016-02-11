# register application
curl -X POST -H "username:$USER" -H "Content-Type: application/json"  -d '{"name": "hbase46", "provider": "SLIDER", "enabled": true}' $HOST/v1/applications | jq .
curl -X GET -H "username:$USER" $HOST/v1/applications/$APPLICATIONID | jq .
curl -X GET -H "username:$USER" $HOST/v1/applications | jq .
curl -X PUT -H "username:$USER" -H "Content-Type: application/json"  -d '{"name": "hbase46", "provider": "SLIDER", "enabled": false}' $HOST/v1/applications/$APPLICATIONID | jq .
curl -X DELETE -H "username:$USER" $HOST/v1/applications/$APPLICATIONID

# create group policy
curl -X POST -H "username:$USER" -H "Content-Type: application/json"  -d '{"name":"scaleOutHBase","description":"scale out HBase regionserver","enabled":true,"interval":60,"timezone":"UTC","cron":"? * MON-FRI","startTime":"9:00:00","duration":"24H00M00S","alert":{"successiveIntervals":2,"and":[{"condition":{"componentName":"HBASE_REGIONSERVER","evalMethod":"PERCENT","threshold":">60","expression":"(last(ProcessTime_mean)*last(ProcessCallTime_num_ops)-first(ProcessTime_mean)*first(ProcessCallTime_num_ops))/(last(ProcessCallTime_num_ops)-first(ProcessCallTime_num_ops))"}},{"condition":{"componentName":"HBASE_REGIONSERVER","evalMethod":"AGGREGATE","expression":"avg(ProcessTime_mean)>200"}}]},"actions":[{"component":"HBASE_REGIONSERVER","adjustmentType":"DELTA_COUNT","scalingAdjustment":{"COUNT":{"min":1,"max":5,"adjustment":1}}}]}' $HOST/v1/applications/$APPLICATIONID/policies/group | jq .
curl -X GET -H "username:$USER" $HOST/v1/applications/$APPLICATIONID/policies/group/$POLICYID | jq .
curl -X GET -H "username:$USER" $HOST/v1/applications/$APPLICATIONID/policies/group | jq .
curl -X PUT -H "username:$USER" -H "Content-Type: application/json"  -d '{"name":"scaleOutHBase","description":"scale out HBase regionserver","enabled":false,"interval":60,"timezone":"UTC","cron":"? * MON-FRI","startTime":"9:00:00","duration":"24H00M00S","alert":{"successiveIntervals":2,"and":[{"condition":{"componentName":"HBASE_REGIONSERVER","evalMethod":"PERCENT","threshold":">60","expression":"(last(ProcessTime_mean)*last(ProcessCallTime_num_ops)-first(ProcessTime_mean)*first(ProcessCallTime_num_ops))/(last(ProcessCallTime_num_ops)-first(ProcessCallTime_num_ops))"}},{"condition":{"componentName":"HBASE_REGIONSERVER","evalMethod":"AGGREGATE","expression":"avg(ProcessTime_mean)>200"}}]},"actions":[{"component":"HBASE_REGIONSERVER","adjustmentType":"DELTA_COUNT","scalingAdjustment":{"COUNT":{"min":1,"max":5,"adjustment":1}}}]}' $HOST/v1/applications/$APPLICATIONID/policies/group/$POLICYID | jq .
curl -X DELETE -H "username:$USER" $HOST/v1/applications/$APPLICATIONID/policies/group/$POLICYID


# create instance policy

# get triggered scaling policy (query history)
curl -X GET -H "username:$USER"  $HOST/v1/applications/$APPLICATIONID/history | jq .
curl -X GET -H "username:$USER"  $HOST/v1/applications/$APPLICATIONID/history/$HISTORYID | jq .
